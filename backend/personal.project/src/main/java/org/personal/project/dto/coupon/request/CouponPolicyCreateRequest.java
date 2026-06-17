package org.personal.project.dto.coupon.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.personal.project.entity.coupon.CouponApplyScope;
import org.personal.project.entity.coupon.CouponIssueType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 쿠폰 정책 생성 요청
 */
public record CouponPolicyCreateRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @NotNull
        CouponIssueType issueType,

        @NotNull
        @Positive
        Integer discountAmount,

        @NotNull
        @PositiveOrZero
        Integer minOrderAmount,

        @NotNull
        CouponApplyScope applyScope,

        @NotNull
        @Positive
        Integer totalIssueLimit,

        @NotNull
        @Positive
        Integer perMemberIssueLimit,

        @NotNull
        @Positive
        Integer perMemberUseLimit,

        @NotNull
        LocalDateTime issueStartAt,

        @NotNull
        LocalDateTime issueEndAt,

        @NotNull
        LocalDateTime useStartAt,

        @NotNull
        LocalDateTime useEndAt,

        @Valid
        List<CouponTargetCreateRequest> targets) {
}
