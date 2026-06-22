package org.personal.project.service.coupon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.dto.coupon.response.CouponApplicabilityResponse;
import org.personal.project.dto.coupon.response.CouponPreviewResponse;
import org.personal.project.entity.CartItem;
import org.personal.project.entity.Product;
import org.personal.project.entity.coupon.CouponApplyScope;
import org.personal.project.entity.coupon.CouponPolicy;
import org.personal.project.entity.coupon.CouponPolicyStatus;
import org.personal.project.entity.coupon.CouponTarget;
import org.personal.project.entity.coupon.CouponTargetType;
import org.personal.project.entity.coupon.MemberCoupon;
import org.personal.project.entity.coupon.MemberCouponStatus;
import org.personal.project.repository.CartItemRepository;
import org.personal.project.repository.coupon.CouponTargetRepository;
import org.personal.project.repository.coupon.MemberCouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 장바구니 기준 쿠폰 적용 계산
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CouponApplyService {

    private static final List<CouponPolicyStatus> USABLE_POLICY_STATUSES = List.of(
            CouponPolicyStatus.ACTIVE,
            CouponPolicyStatus.ISSUE_CLOSED
    );

    private final CartItemRepository cartItemRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponTargetRepository couponTargetRepository;

    /**
     * 현재 장바구니 적용 가능 쿠폰 조회
     */
    @Transactional(readOnly = true)
    public List<CouponApplicabilityResponse> getApplicableCoupons(String memberId) {
        log.info("장바구니 기준 적용 가능 쿠폰 조회 시작 memberId={}", memberId);

        CouponApplyContext context = buildContext(memberId, false);
        if (context.items().isEmpty()) {
            log.info("장바구니 기준 적용 가능 쿠폰 조회 종료 memberId={}, count=0", memberId);
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();
        List<MemberCoupon> coupons = memberCouponRepository.findUsableCoupons(
                memberId,
                MemberCouponStatus.ISSUED,
                USABLE_POLICY_STATUSES,
                now
        );

        Map<Long, List<CouponTarget>> targetMap = findTargetMap(toPolicyIds(coupons));

        List<CouponApplicabilityResponse> response = coupons.stream()
                .filter(coupon -> isMinimumOrderAmountSatisfied(coupon.getPolicy(), context.orderAmount()))
                .map(coupon -> toApplicabilityResponse(coupon, context, targetMap.getOrDefault(
                        coupon.getPolicy().getPolicyId(),
                        List.of()
                )))
                .filter(responseDto -> responseDto.applicableAmount() > 0)
                .toList();

        log.info("장바구니 기준 적용 가능 쿠폰 조회 종료 memberId={}, count={}", memberId, response.size());
        return response;
    }

    /**
     * 쿠폰 적용 미리보기
     */
    @Transactional(readOnly = true)
    public CouponPreviewResponse preview(String memberId, Long memberCouponId) {
        log.info("쿠폰 적용 미리보기 시작 memberId={}, memberCouponId={}", memberId, memberCouponId);

        CouponApplyContext context = buildContext(memberId, true);
        MemberCoupon coupon = memberCouponRepository.findByMemberCouponIdAndMemberEmail(memberCouponId, memberId)
                .orElseThrow(() -> new CouponException("사용자 쿠폰을 찾을 수 없습니다."));

        CouponPolicy policy = coupon.getPolicy();
        validateCouponForPreview(coupon, policy, context);

        List<CouponTarget> targets = couponTargetRepository.findByPolicyPolicyId(policy.getPolicyId());
        int applicableAmount = calculateApplicableAmount(policy, context, targets);
        if (applicableAmount <= 0) {
            throw new CouponException("쿠폰 적용 대상 상품이 없습니다.");
        }

        int discountAmount = calculateDiscountAmount(policy, applicableAmount);
        int payableAmount = Math.max(0, context.orderAmount() - discountAmount);

        log.info("쿠폰 적용 미리보기 종료 memberId={}, memberCouponId={}, discountAmount={}, payableAmount={}",
                memberId, memberCouponId, discountAmount, payableAmount);

        return new CouponPreviewResponse(
                context.orderAmount(),
                discountAmount,
                payableAmount
        );
    }

    /**
     * 쿠폰 적용을 위해 장바구니 계산 정보?자료? 생성
     */
    private CouponApplyContext buildContext(String memberId, boolean failOnEmptyCart) {
        List<CartItem> cartItems = cartItemRepository.findItemsForCheckout(memberId);
        if (cartItems.isEmpty()) {
            if (failOnEmptyCart) {
                throw new CouponException("장바구니가 비어 있습니다.");
            }
            return new CouponApplyContext(memberId, 0, List.of());
        }

        long orderAmount = 0L;
        List<CouponApplyItem> items = cartItems.stream()
                .map(this::toApplyItem)
                .toList();

        for (CouponApplyItem item : items) {
            orderAmount += item.itemAmount();
            assertIntegerRange(orderAmount);
        }

        return new CouponApplyContext(memberId, (int) orderAmount, items);
    }

    /**
     * 장바구니 상품 적용 금액 변환
     */
    private CouponApplyItem toApplyItem(CartItem cartItem) {
        Product product = cartItem.getProduct();
        if (cartItem.getQty() <= 0) {
            throw new CouponException("장바구니 수량이 올바르지 않습니다.");
        }
        if (product.isDelFlag()) {
            throw new CouponException("삭제된 상품은 쿠폰 적용이 불가능합니다. productNo=" + product.getPno());
        }
        if (product.getStock() < cartItem.getQty()) {
            throw new CouponException("재고가 부족한 상품은 쿠폰 적용이 불가능합니다. productNo=" + product.getPno());
        }

        long itemAmount = Math.multiplyExact((long) product.getPrice(), cartItem.getQty());
        assertIntegerRange(itemAmount);

        return new CouponApplyItem(
                product.getPno(),
                (int) itemAmount,
                cartItem.getQty()
        );
    }

    /**
     * 적용 가능 쿠폰 응답 변환
     */
    private CouponApplicabilityResponse toApplicabilityResponse(
            MemberCoupon coupon,
            CouponApplyContext context,
            List<CouponTarget> targets
    ) {
        CouponPolicy policy = coupon.getPolicy();
        int applicableAmount = calculateApplicableAmount(policy, context, targets);
        int discountAmount = calculateDiscountAmount(policy, applicableAmount);

        return new CouponApplicabilityResponse(
                coupon.getMemberCouponId(),
                policy.getPolicyId(),
                policy.getName(),
                policy.getIssueType().name(),
                coupon.getStatus().name(),
                policy.getStatus().name(),
                policy.getDiscountAmount(),
                discountAmount,
                applicableAmount,
                policy.getMinOrderAmount(),
                policy.getApplyScope().name(),
                policy.getUseStartAt(),
                policy.getUseEndAt()
        );
    }

    /**
     * 미리보기 쿠폰 상태 검증
     */
    private void validateCouponForPreview(
            MemberCoupon coupon,
            CouponPolicy policy,
            CouponApplyContext context
    ) {
        if (coupon.getStatus() != MemberCouponStatus.ISSUED) {
            throw new CouponException("사용 가능한 쿠폰이 아닙니다.");
        }
        if (!USABLE_POLICY_STATUSES.contains(policy.getStatus())) {
            throw new CouponException("사용 가능한 쿠폰 정책이 아닙니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (policy.getUseStartAt().isAfter(now) || !policy.getUseEndAt().isAfter(now)) {
            throw new CouponException("쿠폰 사용 기간이 아닙니다.");
        }
        if (!isMinimumOrderAmountSatisfied(policy, context.orderAmount())) {
            throw new CouponException("최소 주문 금액을 충족하지 못했습니다.");
        }
    }

    /**
     * 적용 대상 금액 계산
     */
    private int calculateApplicableAmount(
            CouponPolicy policy,
            CouponApplyContext context,
            List<CouponTarget> targets
    ) {
        if (policy.getApplyScope() == CouponApplyScope.ORDER) {
            return context.orderAmount();
        }
        if (policy.getApplyScope() == CouponApplyScope.PRODUCT) {
            return calculateProductApplicableAmount(context, targets);
        }

        return 0;
    }

    /**
     * 상품 대상 적용 금액 계산
     */
    private int calculateProductApplicableAmount(CouponApplyContext context, List<CouponTarget> targets) {
        List<Long> productIds = targets.stream()
                .filter(target -> target.getTargetType() == CouponTargetType.PRODUCT)
                .map(CouponTarget::getTargetRefId)
                .toList();

        long applicableAmount = context.items().stream()
                .filter(item -> productIds.contains(item.productId()))
                .mapToLong(CouponApplyItem::itemAmount)
                .sum();

        assertIntegerRange(applicableAmount);
        return (int) applicableAmount;
    }

    /**
     * 정액 할인 금액 계산
     */
    private int calculateDiscountAmount(CouponPolicy policy, int applicableAmount) {
        if (applicableAmount <= 0) {
            return 0;
        }
        return Math.min(policy.getDiscountAmount(), applicableAmount);
    }

    /**
     * 최소 주문 금액 충족 여부
     */
    private boolean isMinimumOrderAmountSatisfied(CouponPolicy policy, int orderAmount) {
        return orderAmount >= policy.getMinOrderAmount();
    }

    /**
     * 정책 ID 목록 변환
     */
    private List<Long> toPolicyIds(List<MemberCoupon> coupons) {
        return coupons.stream()
                .map(coupon -> coupon.getPolicy().getPolicyId())
                .toList();
    }

    /**
     * 정책별 적용 대상 조회
     */
    private Map<Long, List<CouponTarget>> findTargetMap(Collection<Long> policyIds) {
        if (policyIds.isEmpty()) {
            return Map.of();
        }

        return couponTargetRepository.findByPolicyPolicyIdIn(policyIds).stream()
                .collect(Collectors.groupingBy(target -> target.getPolicy().getPolicyId()));
    }

    /**
     * 금액 범위 검증
     *
     */
    private void assertIntegerRange(long amount) {
        if (amount > Integer.MAX_VALUE) {
            throw new CouponException("주문 금액이 허용 범위를 초과했습니다.");
        }
    }
}
