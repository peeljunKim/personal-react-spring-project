package org.personal.project.dto.coupon.response;

import java.time.LocalDateTime;

/**
 * 사용자 쿠폰 응답
 */
public record MemberCouponResponse(
        Long memberCouponId,
        Long policyId,
        String policyName,
        String issueType,
        String couponStatus,
        String policyStatus,
        Integer discountAmount,
        Integer minOrderAmount,
        String applyScope,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt,
        LocalDateTime issuedAt,
        LocalDateTime reservedAt,
        LocalDateTime usedAt,
        LocalDateTime expiredAt,
        LocalDateTime canceledAt) {
}
