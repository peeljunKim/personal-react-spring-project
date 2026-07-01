package org.personal.project.entity.coupon;

/**
 * 주문 쿠폰 상태
 */
public enum OrderCouponStatus {
    RESERVED,  // 쿠폰 적용 대기
    CONFIRMED, // 쿠폰 확정
    RELEASED,  // 쿠폰 적용 해제
    RESTORED   // 취소/환불 후 복구
}
