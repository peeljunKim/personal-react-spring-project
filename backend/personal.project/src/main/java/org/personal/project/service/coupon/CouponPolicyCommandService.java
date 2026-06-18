package org.personal.project.service.coupon;

import lombok.RequiredArgsConstructor;
import org.personal.project.dto.coupon.request.CouponPolicyCreateRequest;
import org.personal.project.dto.coupon.request.CouponPolicyUpdateRequest;
import org.personal.project.dto.coupon.request.CouponTargetCreateRequest;
import org.personal.project.dto.coupon.response.CouponPolicyCreateResponse;
import org.personal.project.dto.coupon.response.CouponPolicyDetailResponse;
import org.personal.project.dto.coupon.response.CouponPolicyStatusResponse;
import org.personal.project.entity.coupon.CouponApplyScope;
import org.personal.project.entity.coupon.CouponPolicy;
import org.personal.project.entity.coupon.CouponPolicyStatus;
import org.personal.project.entity.coupon.CouponTarget;
import org.personal.project.entity.coupon.CouponTargetType;
import org.personal.project.entity.coupon.MemberCouponStatus;
import org.personal.project.repository.coupon.CouponPolicyRepository;
import org.personal.project.repository.coupon.CouponTargetRepository;
import org.personal.project.repository.coupon.MemberCouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 쿠폰 정책 처리
 */
@Service
@RequiredArgsConstructor
public class CouponPolicyCommandService {

    private final CouponPolicyRepository couponPolicyRepository;
    private final CouponTargetRepository couponTargetRepository;
    private final MemberCouponRepository memberCouponRepository;

    /**
     * 쿠폰 정책 생성
     */
    @Transactional
    public CouponPolicyCreateResponse createPolicy(CouponPolicyCreateRequest request) {
        List<CouponTargetCreateRequest> targets = normalizeTargets(request.targets());
        validatePeriod(request.issueStartAt(), request.issueEndAt(), request.useStartAt(), request.useEndAt());
        validateTargets(request.applyScope(), targets);

        CouponPolicy policy = couponPolicyRepository.save(CouponPolicy.builder()
                .name(request.name())
                .issueType(request.issueType())
                .discountAmount(request.discountAmount())
                .minOrderAmount(request.minOrderAmount())
                .applyScope(request.applyScope())
                .totalIssueLimit(request.totalIssueLimit())
                .issuedCount(0)
                .perMemberIssueLimit(request.perMemberIssueLimit())
                .perMemberUseLimit(request.perMemberUseLimit())
                .issueStartAt(request.issueStartAt())
                .issueEndAt(request.issueEndAt())
                .useStartAt(request.useStartAt())
                .useEndAt(request.useEndAt())
                .status(CouponPolicyStatus.DRAFT)
                .build());

        if (!targets.isEmpty()) {
            List<CouponTarget> couponTargets = targets.stream()
                    .map(target -> CouponTarget.builder()
                            .policy(policy)
                            .targetType(target.targetType())
                            .targetRefId(target.targetRefId())
                            .build())
                    .toList();
            couponTargetRepository.saveAll(couponTargets);
        }

        return new CouponPolicyCreateResponse(
                policy.getPolicyId(),
                policy.getName(),
                policy.getIssueType().name(),
                policy.getStatus().name(),
                "쿠폰 정책 생성 완료"
        );
    }

    /**
     * 쿠폰 정책 수정
     */
    @Transactional
    public CouponPolicyDetailResponse updatePolicy(Long policyId, CouponPolicyUpdateRequest request) {
        CouponPolicy policy = getPolicy(policyId);
        validateStatus(policy, CouponPolicyStatus.DRAFT);

        List<CouponTargetCreateRequest> targets = normalizeTargets(request.targets());
        validatePeriod(request.issueStartAt(), request.issueEndAt(), request.useStartAt(), request.useEndAt());
        validateTargets(request.applyScope(), targets);

        policy.updateDraft(
                request.name(),
                request.issueType(),
                request.discountAmount(),
                request.minOrderAmount(),
                request.applyScope(),
                request.totalIssueLimit(),
                request.perMemberIssueLimit(),
                request.perMemberUseLimit(),
                request.issueStartAt(),
                request.issueEndAt(),
                request.useStartAt(),
                request.useEndAt()
        );

        couponTargetRepository.deleteByPolicyPolicyId(policyId);
        couponTargetRepository.flush();
        saveTargets(policy, targets);

        return CouponPolicyMapper.toDetailResponse(
                policy,
                couponTargetRepository.findByPolicyPolicyId(policyId)
        );
    }

    /**
     * 쿠폰 정책 활성화
     */
    @Transactional
    public CouponPolicyStatusResponse activate(Long policyId) {
        CouponPolicy policy = getPolicy(policyId);
        validateStatus(policy, CouponPolicyStatus.DRAFT);
        policy.activate();
        return toStatusResponse(policy, 0, "쿠폰 정책 활성화 완료");
    }

    /**
     * 쿠폰 정책 일시 중지
     */
    @Transactional
    public CouponPolicyStatusResponse pause(Long policyId) {
        CouponPolicy policy = getPolicy(policyId);
        validateStatus(policy, CouponPolicyStatus.ACTIVE);
        policy.pause();
        return toStatusResponse(policy, 0, "쿠폰 정책 일시 중지 완료");
    }

    /**
     * 쿠폰 정책 재개
     */
    @Transactional
    public CouponPolicyStatusResponse resume(Long policyId) {
        CouponPolicy policy = getPolicy(policyId);
        validateStatus(policy, CouponPolicyStatus.PAUSED);
        policy.resume();
        return toStatusResponse(policy, 0, "쿠폰 정책 재개 완료");
    }

    /**
     * 쿠폰 발급 종료
     */
    @Transactional
    public CouponPolicyStatusResponse closeIssue(Long policyId) {
        CouponPolicy policy = getPolicy(policyId);
        validateStatus(policy, CouponPolicyStatus.ACTIVE, CouponPolicyStatus.PAUSED);
        policy.closeIssue();
        return toStatusResponse(policy, 0, "쿠폰 발급 종료 완료");
    }

    /**
     * 쿠폰 정책 취소
     */
    @Transactional
    public CouponPolicyStatusResponse cancel(Long policyId) {
        CouponPolicy policy = getPolicy(policyId);
        validateStatus(policy,
                CouponPolicyStatus.DRAFT,
                CouponPolicyStatus.ACTIVE,
                CouponPolicyStatus.PAUSED,
                CouponPolicyStatus.ISSUE_CLOSED);

        boolean hasReservedCoupon = memberCouponRepository.existsByPolicyPolicyIdAndStatus(
                policyId,
                MemberCouponStatus.RESERVED
        );
        if (hasReservedCoupon) {
            throw new CouponException("예약 중인 쿠폰이 있어 정책을 취소할 수 없습니다.");
        }

        int affectedCouponCount = memberCouponRepository.cancelIssuedCouponsByPolicyId(
                policyId,
                MemberCouponStatus.ISSUED,
                MemberCouponStatus.CANCELED,
                LocalDateTime.now()
        );
        policy.cancel();

        return toStatusResponse(policy, affectedCouponCount, "쿠폰 정책 취소 완료");
    }

    /**
     * 쿠폰 정책 조회
     */
    private CouponPolicy getPolicy(Long policyId) {
        return couponPolicyRepository.findById(policyId)
                .orElseThrow(() -> new CouponException("쿠폰 정책을 찾을 수 없습니다. policyId=" + policyId));
    }

    /**
     * 정책 상태 검증
     */
    private void validateStatus(CouponPolicy policy, CouponPolicyStatus... allowedStatuses) {
        for (CouponPolicyStatus allowedStatus : allowedStatuses) {
            if (policy.getStatus() == allowedStatus) {
                return;
            }
        }
        throw new CouponException("현재 상태에서는 처리할 수 없는 쿠폰 정책입니다. status=" + policy.getStatus());
    }

    /**
     * 정책 상태 응답 변환
     */
    private CouponPolicyStatusResponse toStatusResponse(
            CouponPolicy policy,
            Integer affectedCouponCount,
            String message
    ) {
        return new CouponPolicyStatusResponse(
                policy.getPolicyId(),
                policy.getStatus().name(),
                affectedCouponCount,
                message
        );
    }

    /**
     * 적용 대상 목록 정규화
     */
    private List<CouponTargetCreateRequest> normalizeTargets(List<CouponTargetCreateRequest> targets) {
        return targets == null ? List.of() : targets;
    }

    /**
     * 적용 대상 저장
     */
    private void saveTargets(CouponPolicy policy, List<CouponTargetCreateRequest> targets) {
        if (targets.isEmpty()) {
            return;
        }

        List<CouponTarget> couponTargets = targets.stream()
                .map(target -> CouponTarget.builder()
                        .policy(policy)
                        .targetType(target.targetType())
                        .targetRefId(target.targetRefId())
                        .build())
                .toList();
        couponTargetRepository.saveAll(couponTargets);
    }

    /**
     * 발급/사용 기간 검증
     */
    private void validatePeriod(
            LocalDateTime issueStartAt,
            LocalDateTime issueEndAt,
            LocalDateTime useStartAt,
            LocalDateTime useEndAt
    ) {
        if (!issueStartAt.isBefore(issueEndAt)) {
            throw new CouponException("쿠폰 발급 시작 시간은 종료 시간보다 빨라야 합니다.");
        }
        if (!useStartAt.isBefore(useEndAt)) {
            throw new CouponException("쿠폰 사용 시작 시간은 종료 시간보다 빨라야 합니다.");
        }
        if (issueStartAt.isAfter(useEndAt)) {
            throw new CouponException("쿠폰 발급 시작 시간은 사용 종료 시간보다 늦을 수 없습니다.");
        }
    }

    /**
     * 적용 대상 검증
     */
    private void validateTargets(CouponApplyScope applyScope, List<CouponTargetCreateRequest> targets) {
        if (applyScope == CouponApplyScope.ORDER) {
            if (!targets.isEmpty()) {
                throw new CouponException("전체 주문 쿠폰은 적용 대상을 가질 수 없습니다.");
            }
            return;
        }

        if (targets.isEmpty()) {
            throw new CouponException("상품/카테고리 쿠폰은 적용 대상이 필요합니다.");
        }

        CouponTargetType expectedTargetType = toTargetType(applyScope);
        Set<String> uniqueTargets = new HashSet<>();
        for (CouponTargetCreateRequest target : targets) {
            if (target.targetType() != expectedTargetType) {
                throw new CouponException("쿠폰 적용 범위와 대상 타입이 일치하지 않습니다.");
            }
            String uniqueKey = target.targetType().name() + ":" + target.targetRefId();
            if (!uniqueTargets.add(uniqueKey)) {
                throw new CouponException("쿠폰 적용 대상이 중복되었습니다.");
            }
        }
    }

    /**
     * 적용 범위 대상 타입 변환
     */
    private CouponTargetType toTargetType(CouponApplyScope applyScope) {
        if (applyScope == CouponApplyScope.PRODUCT) {
            return CouponTargetType.PRODUCT;
        }
        if (applyScope == CouponApplyScope.CATEGORY) {
            return CouponTargetType.CATEGORY;
        }
        throw new CouponException("지원하지 않는 쿠폰 적용 범위입니다.");
    }
}
