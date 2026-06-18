package org.personal.project.dto.coupon.response;

/**
 * 쿠폰 적용 대상 응답
 */
public record CouponTargetResponse(
        Long targetId,
        String targetType,
        Long targetRefId) {
}
