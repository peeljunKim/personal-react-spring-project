package org.personal.project.entity.coupon;

/**
 * 사용자 쿠폰 상태
 */
public enum MemberCouponStatus {
    ISSUED,   // 발급 완료
    RESERVED, // 주문/결제 예약
    USED,     // 사용 완료
    EXPIRED,  // 사용 기간 만료
    CANCELED  // 쿠폰 회수
}
