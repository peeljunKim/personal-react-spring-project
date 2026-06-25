package org.personal.project.service.coupon;

import org.personal.project.entity.Order;
import org.personal.project.entity.coupon.CouponPolicy;
import org.personal.project.entity.coupon.CouponUsageHistory;
import org.personal.project.entity.coupon.MemberCoupon;
import org.personal.project.entity.coupon.OrderCoupon;
import org.springframework.stereotype.Component;

/**
 * 쿠폰 사용 이력 생성 (사용 및 취소)
 * <p>
 * CouponUsageHistory 테이블은 고의적으로 연관관계가 아님
 * 그래서 create() 메소드를 CouponUsageHistory 클래스에 안 넣고 해당 클래스에 넣어둠
 */
@Component
public class CouponUsageHistoryFactory {

    private static final String EVENT_USED = "USED";
    private static final String EVENT_RELEASED = "RELEASED";
    private static final String REASON_PAYMENT_CONFIRMED = "PAYMENT_CONFIRMED";

    /**
     * 쿠폰 사용 확정 이력 생성
     */
    public CouponUsageHistory used(OrderCoupon orderCoupon) {
        return create(orderCoupon, EVENT_USED, REASON_PAYMENT_CONFIRMED);
    }

    /**
     * 쿠폰 결제 취소 이력 생성
     */
    public CouponUsageHistory released(OrderCoupon orderCoupon, String reason) {
        return create(orderCoupon, EVENT_RELEASED, reason);
    }

    /**
     * 주문 쿠폰 기준 스냅샷 생성
     */
    private CouponUsageHistory create(
            OrderCoupon orderCoupon,
            String eventType,
            String reason
    ) {
        MemberCoupon memberCoupon = orderCoupon.getMemberCoupon();
        CouponPolicy policy = memberCoupon.getPolicy();
        Order order = orderCoupon.getOrder();

        return CouponUsageHistory.builder()
                .memberCouponId(memberCoupon.getMemberCouponId())
                .policyId(policy.getPolicyId())
                .policyName(policy.getName())
                .discountAmount(orderCoupon.getDiscountAmount())
                .memberId(memberCoupon.getMember().getEmail())
                .orderId(order.getOno())
                .paymentId(order.getPaymentId())
                .eventType(eventType)
                .amount(orderCoupon.getDiscountAmount())
                .reason(reason)
                .build();
    }
}
