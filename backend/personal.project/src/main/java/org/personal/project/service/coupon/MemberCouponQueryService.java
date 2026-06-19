package org.personal.project.service.coupon;

import lombok.RequiredArgsConstructor;
import org.personal.project.dto.coupon.response.CouponApplicabilityResponse;
import org.personal.project.dto.coupon.response.MemberCouponResponse;
import org.personal.project.dto.page.PageRequestDTO;
import org.personal.project.dto.page.PageResponseDTO;
import org.personal.project.entity.coupon.CouponApplyScope;
import org.personal.project.entity.coupon.CouponPolicy;
import org.personal.project.entity.coupon.CouponPolicyStatus;
import org.personal.project.entity.coupon.MemberCoupon;
import org.personal.project.entity.coupon.MemberCouponStatus;
import org.personal.project.repository.coupon.MemberCouponRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자/관리자 쿠폰 조회 처리
 */
@Service
@RequiredArgsConstructor
public class MemberCouponQueryService {

    private static final List<CouponPolicyStatus> USABLE_POLICY_STATUSES = List.of(
            CouponPolicyStatus.ACTIVE,
            CouponPolicyStatus.ISSUE_CLOSED
    );

    private final MemberCouponRepository memberCouponRepository;

    /**
     * 지금 사용 가능 쿠폰 조회
     */
    @Transactional(readOnly = true)
    public List<MemberCouponResponse> getMyCoupons(String memberId) {
        List<MemberCoupon> result = memberCouponRepository.findUsableCoupons(
                memberId,
                MemberCouponStatus.ISSUED,
                USABLE_POLICY_STATUSES,
                LocalDateTime.now()
        );

        return toMemberCouponList(result);
    }

    /**
     * 금액 기준 적용 가능 쿠폰 조회
     */
    @Transactional(readOnly = true)
    public List<CouponApplicabilityResponse> getApplicableCoupons(
            String memberId,
            Integer orderAmount
    ) {
        List<MemberCoupon> result = memberCouponRepository.findApplicableCouponsByAmount(
                memberId,
                MemberCouponStatus.ISSUED,
                USABLE_POLICY_STATUSES,
                LocalDateTime.now(),
                orderAmount
        );

        return result.stream()
                .map(this::toApplicabilityResponse)
                .toList();
    }

    /**
     * 관리자 사용자 쿠폰 전체 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<MemberCouponResponse> getAdminMemberCoupons(
            String memberId,
            MemberCouponStatus status,
            Long policyId,
            PageRequestDTO pageRequestDTO
    ) {
        Page<MemberCoupon> result = memberCouponRepository.findAdminMemberCoupons(
                memberId,
                status,
                policyId,
                toPageable(pageRequestDTO)
        );

        return toMemberCouponPage(result, pageRequestDTO);
    }

    /**
     * 사용자 쿠폰 목록 변환
     */
    private List<MemberCouponResponse> toMemberCouponList(List<MemberCoupon> result) {
        return result.stream()
                .map(this::toMemberCouponResponse)
                .toList();
    }

    /**
     * 관리자 사용자 쿠폰 페이지 변환
     */
    private PageResponseDTO<MemberCouponResponse> toMemberCouponPage(
            Page<MemberCoupon> result,
            PageRequestDTO pageRequestDTO
    ) {
        List<MemberCouponResponse> dtoList = result.getContent().stream()
                .map(this::toMemberCouponResponse)
                .toList();

        return PageResponseDTO.<MemberCouponResponse>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageRequestDTO)
                .totalCount(result.getTotalElements())
                .build();
    }

    /**
     * 사용자 쿠폰 응답 변환
     */
    private MemberCouponResponse toMemberCouponResponse(MemberCoupon coupon) {
        CouponPolicy policy = coupon.getPolicy();
        return new MemberCouponResponse(
                coupon.getMemberCouponId(),
                policy.getPolicyId(),
                policy.getName(),
                policy.getIssueType().name(),
                coupon.getStatus().name(),
                policy.getStatus().name(),
                policy.getDiscountAmount(),
                policy.getMinOrderAmount(),
                policy.getApplyScope().name(),
                policy.getUseStartAt(),
                policy.getUseEndAt(),
                coupon.getIssuedAt(),
                coupon.getReservedAt(),
                coupon.getUsedAt(),
                coupon.getExpiredAt(),
                coupon.getCanceledAt()
        );
    }

    /**
     * 적용 가능 쿠폰 응답 변환
     */
    private CouponApplicabilityResponse toApplicabilityResponse(MemberCoupon coupon) {
        CouponPolicy policy = coupon.getPolicy();
        return new CouponApplicabilityResponse(
                coupon.getMemberCouponId(),
                policy.getPolicyId(),
                policy.getName(),
                policy.getIssueType().name(),
                coupon.getStatus().name(),
                policy.getStatus().name(),
                policy.getDiscountAmount(),
                policy.getMinOrderAmount(),
                policy.getApplyScope().name(),
                policy.getApplyScope() != CouponApplyScope.ORDER,
                policy.getUseStartAt(),
                policy.getUseEndAt()
        );
    }

    /**
     * 페이지 요청 변환
     */
    private Pageable toPageable(PageRequestDTO pageRequestDTO) {
        int page = Math.max(pageRequestDTO.getPage(), 1) - 1;
        int size = Math.max(pageRequestDTO.getSize(), 1);
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "memberCouponId"));
    }
}
