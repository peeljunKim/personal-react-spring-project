package org.personal.project.dto.coupon.response;

/**
 * 쿠폰 미리보기 응답
 */
public record CouponPreviewResponse(
        Integer originalAmount,
        Integer discountAmount,
        Integer payableAmount
) {
}
