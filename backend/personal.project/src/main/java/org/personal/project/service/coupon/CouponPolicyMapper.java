package org.personal.project.service.coupon;

import org.personal.project.dto.coupon.response.CouponPolicyDetailResponse;
import org.personal.project.dto.coupon.response.CouponPolicySummaryResponse;
import org.personal.project.dto.coupon.response.CouponTargetResponse;
import org.personal.project.entity.coupon.CouponPolicy;
import org.personal.project.entity.coupon.CouponTarget;

import java.util.List;

/**
 * 쿠폰 정책 응답 변환
 */
final class CouponPolicyMapper {

    private CouponPolicyMapper() {
    }

    /**
     * 정책 목록 응답 변환
     */
    static CouponPolicySummaryResponse toSummaryResponse(CouponPolicy policy) {
        return new CouponPolicySummaryResponse(
                policy.getPolicyId(),
                policy.getName(),
                policy.getIssueType().name(),
                policy.getStatus().name(),
                policy.getDiscountAmount(),
                policy.getMinOrderAmount(),
                policy.getApplyScope().name(),
                policy.getTotalIssueLimit(),
                policy.getIssuedCount(),
                policy.getIssueStartAt(),
                policy.getIssueEndAt(),
                policy.getUseStartAt(),
                policy.getUseEndAt(),
                policy.getCreatedAt(),
                policy.getUpdatedAt()
        );
    }

    /**
     * 정책 상세 응답 변환
     */
    static CouponPolicyDetailResponse toDetailResponse(CouponPolicy policy, List<CouponTarget> targets) {
        return new CouponPolicyDetailResponse(
                policy.getPolicyId(),
                policy.getName(),
                policy.getIssueType().name(),
                policy.getStatus().name(),
                policy.getDiscountAmount(),
                policy.getMinOrderAmount(),
                policy.getApplyScope().name(),
                policy.getTotalIssueLimit(),
                policy.getIssuedCount(),
                policy.getPerMemberIssueLimit(),
                policy.getPerMemberUseLimit(),
                policy.getIssueStartAt(),
                policy.getIssueEndAt(),
                policy.getUseStartAt(),
                policy.getUseEndAt(),
                targets.stream()
                        .map(CouponPolicyMapper::toTargetResponse)
                        .toList(),
                policy.getCreatedAt(),
                policy.getUpdatedAt()
        );
    }

    /**
     * 적용 대상 응답 변환
     */
    private static CouponTargetResponse toTargetResponse(CouponTarget target) {
        return new CouponTargetResponse(
                target.getTargetId(),
                target.getTargetType().name(),
                target.getTargetRefId()
        );
    }
}
