package org.personal.project.dto.coupon.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.personal.project.entity.coupon.CouponTargetType;

/**
 * 쿠폰 적용 대상 생성 요청
 */
public record CouponTargetCreateRequest(
        @NotNull
        CouponTargetType targetType,

        @NotNull
        @Positive
        Long targetRefId) {
}
