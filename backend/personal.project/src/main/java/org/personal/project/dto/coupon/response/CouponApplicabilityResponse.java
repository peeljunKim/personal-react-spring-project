package org.personal.project.dto.coupon.response;

import java.time.LocalDateTime;

/**
 * 적용 가능 쿠폰 응답
 */
public record CouponApplicabilityResponse(
        Long memberCouponId,
        Long policyId,
        String policyName,
        String issueType,
        String couponStatus,
        String policyStatus,
        Integer discountAmount,
        Integer minOrderAmount,
        String applyScope,
        boolean requiresItemCheck,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt) {
}
