package org.personal.project.dto.coupon.response;

/**
 * 이벤트 쿠폰 발급 접수 응답
 */
public record EventCouponIssueResponse(
        String requestKey,
        Long policyId,
        String status,
        String message) {
}
