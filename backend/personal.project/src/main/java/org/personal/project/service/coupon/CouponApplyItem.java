package org.personal.project.service.coupon;

/**
 * 쿠폰 적용 상품 정보
 */
record CouponApplyItem(
        Long productId,
        Integer itemAmount,
        Integer quantity
) {
}
