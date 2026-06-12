package org.personal.project.entity.coupon;

/**
 * 주문 쿠폰 상태
 */
public enum OrderCouponStatus {
    RESERVED,  // 주문 쿠폰 예약
    CONFIRMED, // 주문 쿠폰 확정
    RELEASED,  // 주문 쿠폰 예약 해제
    RESTORED   // 취소/환불 후 복구
}
