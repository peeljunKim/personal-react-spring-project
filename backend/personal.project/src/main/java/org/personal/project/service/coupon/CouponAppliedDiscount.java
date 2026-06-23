package org.personal.project.service.coupon;

import org.personal.project.entity.coupon.MemberCoupon;

/**
 * 결제 준비 및 쿠폰 할인 결과
 */
public record CouponAppliedDiscount(
        MemberCoupon memberCoupon,  // 예약 대상 사용자 쿠폰
        Integer originalAmount,  // 쿠폰 적용 전 금액
        Integer discountAmount,  // 쿠폰 할인 금액
        Integer payableAmount  // 쿠폰 적용 후 결제 금액
) {
}
