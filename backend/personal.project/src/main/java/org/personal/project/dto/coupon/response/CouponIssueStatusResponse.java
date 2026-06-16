package org.personal.project.dto.coupon.response;

/**
 * 이벤트 쿠폰 발급 상태 응답
 */
public record CouponIssueStatusResponse(
        String requestKey,
        Long policyId,
        String memberId,
        String status,
        String failureReason) {
}
