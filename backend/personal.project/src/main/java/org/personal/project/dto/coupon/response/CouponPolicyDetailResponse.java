package org.personal.project.dto.coupon.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 쿠폰 정책 상세 응답
 */
public record CouponPolicyDetailResponse(
        Long policyId,
        String name,
        String issueType,
        String status,
        Integer discountAmount,
        Integer minOrderAmount,
        String applyScope,
        Integer totalIssueLimit,
        Integer issuedCount,
        Integer perMemberIssueLimit,
        Integer perMemberUseLimit,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt,
        List<CouponTargetResponse> targets,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
