package org.personal.project.dto.coupon;

/**
 * 이벤트 쿠폰 발급 메시지
 */
public record CouponIssueMessage(
        String requestKey,
        Long policyId,
        String memberId) {
}
