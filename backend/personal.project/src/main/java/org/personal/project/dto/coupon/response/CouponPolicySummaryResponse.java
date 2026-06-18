package org.personal.project.dto.coupon.response;

import java.time.LocalDateTime;

/**
 * 쿠폰 정책 목록 응답
 */
public record CouponPolicySummaryResponse(
        Long policyId,
        String name,
        String issueType,
        String status,
        Integer discountAmount,
        Integer minOrderAmount,
        String applyScope,
        Integer totalIssueLimit,
        Integer issuedCount,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
