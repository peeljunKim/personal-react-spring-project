package org.personal.project.dto.coupon.response;

/**
 * 쿠폰 정책 상태 변경 응답
 */
public record CouponPolicyStatusResponse(
        Long policyId,
        String status,
        Integer affectedCouponCount,
        String message) {
}
