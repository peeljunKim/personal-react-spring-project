package org.personal.project.entity.coupon;

/**
 * 쿠폰 적용 범위
 */
public enum CouponApplyScope {
    ORDER,   // 전체 주문 적용
    PRODUCT, // 특정 상품 적용(아직 카테고리 구현이 안되어 있음)
    CATEGORY // 특정 카테고리 적용
}
