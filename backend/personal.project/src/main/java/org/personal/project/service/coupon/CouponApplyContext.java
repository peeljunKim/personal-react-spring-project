package org.personal.project.service.coupon;

import java.util.List;

/**
 * 쿠폰 적용 계산에 필요한 값
 */
record CouponApplyContext(
        String memberId,
        Integer orderAmount,  // 쿠폰 적용 전 금액
        List<CouponApplyItem> items
) {
}
