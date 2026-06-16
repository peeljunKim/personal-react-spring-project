package org.personal.project.dto.coupon.response;

/**
 * 일반 쿠폰 발급 응답
 */
public record CouponIssueResponse(
        Long memberCouponId,
        Long policyId,
        String status,
        String message) {
}
