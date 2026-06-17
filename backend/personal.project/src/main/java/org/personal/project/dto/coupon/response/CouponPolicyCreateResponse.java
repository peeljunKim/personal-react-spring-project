package org.personal.project.dto.coupon.response;

/**
 * 쿠폰 정책 생성 응답
 */
public record CouponPolicyCreateResponse(
        Long policyId,
        String name,
        String issueType,
        String status,
        String message) {
}
