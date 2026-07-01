package org.personal.project.service.coupon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.entity.coupon.CouponUsageHistory;
import org.personal.project.entity.coupon.MemberCouponStatus;
import org.personal.project.entity.coupon.OrderCoupon;
import org.personal.project.entity.coupon.OrderCouponStatus;
import org.personal.project.exception.CouponException;
import org.personal.project.repository.coupon.CouponUsageHistoryRepository;
import org.personal.project.repository.coupon.MemberCouponRepository;
import org.personal.project.repository.coupon.OrderCouponRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 결제 관련 쿠폰 처리
 * <p>
 * 결제 성공 시 사용자 쿠폰을 사용 확정하고, 결제 실패/취소 시 주문 쿠폰 적용 대기와 Redis 예약을 해제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponPaymentService {

    private final OrderCouponRepository orderCouponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponUsageHistoryRepository couponUsageHistoryRepository;
    private final CouponUsageHistoryFactory couponUsageHistoryFactory;
    private final CouponReservationService couponReservationService;

    /**
     * 쿠폰 사용 확정
     */
    public void confirmCouponUse(Long orderId) {
        OrderCoupon orderCoupon = orderCouponRepository.findForHistoryByOrderOno(orderId)
                .orElse(null);

        if (orderCoupon == null) {
            log.debug("쿠폰 미사용 주문입니다. orderId={}", orderId);
            return;
        }
        if (orderCoupon.getStatus() == OrderCouponStatus.CONFIRMED) {
            log.info("이미 쿠폰 사용 확정된 주문입니다. orderId={}", orderId);
            return;
        }
        if (orderCoupon.getStatus() != OrderCouponStatus.RESERVED) {
            throw new CouponException("확정할 수 없는 주문 쿠폰 상태입니다. orderId=" + orderId
                    + ", status=" + orderCoupon.getStatus());
        }

        Long memberCouponId = orderCoupon.getMemberCoupon().getMemberCouponId();
        String memberId = orderCoupon.getMemberCoupon().getMember().getEmail();
        String paymentId = orderCoupon.getOrder().getPaymentId();
        CouponUsageHistory history = couponUsageHistoryFactory.used(orderCoupon);
        LocalDateTime now = LocalDateTime.now();

        couponReservationService.validateReservation(memberCouponId, orderId, paymentId);

        int memberUpdated = memberCouponRepository.confirmUseIfIssued(
                memberCouponId,
                memberId,
                MemberCouponStatus.ISSUED,
                MemberCouponStatus.USED,
                now
        );
        if (memberUpdated != 1) {
            throw new CouponException("예약 쿠폰 사용 확정에 실패했습니다. memberCouponId="
                    + memberCouponId + ", orderId=" + orderId);
        }

        int orderUpdated = orderCouponRepository.markConfirmedIfReserved(
                orderId,
                OrderCouponStatus.RESERVED,
                OrderCouponStatus.CONFIRMED,
                now
        );
        if (orderUpdated != 1) {
            throw new CouponException("주문 쿠폰 확정에 실패했습니다. orderId=" + orderId);
        }

        couponUsageHistoryRepository.save(history);
        couponReservationService.releaseByPayment(memberCouponId, paymentId);
    }

    /**
     * 주문 쿠폰 적용 대기 해제
     */
    public void releaseCouponReservation(Long orderId, String reason) {
        OrderCoupon orderCoupon = orderCouponRepository.findForHistoryByOrderOno(orderId)
                .orElse(null);
        if (orderCoupon == null) {
            log.debug("해제할 쿠폰이 없는 주문입니다. orderId={}", orderId);
            return;
        }
        if (orderCoupon.getStatus() == OrderCouponStatus.RELEASED) {
            couponReservationService.releaseByPayment(
                    orderCoupon.getMemberCoupon().getMemberCouponId(),
                    orderCoupon.getOrder().getPaymentId()
            );
            log.info("이미 쿠폰 예약 해제된 주문입니다. orderId={}", orderId);
            return;
        }
        if (orderCoupon.getStatus() != OrderCouponStatus.RESERVED) {
            throw new CouponException("해제할 수 없는 주문 쿠폰 상태입니다. orderId=" + orderId
                    + ", status=" + orderCoupon.getStatus());
        }

        Long memberCouponId = orderCoupon.getMemberCoupon().getMemberCouponId();
        String paymentId = orderCoupon.getOrder().getPaymentId();
        CouponUsageHistory history = couponUsageHistoryFactory.released(orderCoupon, reason);
        LocalDateTime now = LocalDateTime.now();

        int orderUpdated = orderCouponRepository.markReleasedIfReserved(
                orderId,
                OrderCouponStatus.RESERVED,
                OrderCouponStatus.RELEASED,
                now
        );
        if (orderUpdated != 1) {
            throw new CouponException("주문 쿠폰 해제에 실패했습니다. orderId=" + orderId);
        }

        couponUsageHistoryRepository.save(history);
        couponReservationService.releaseByPayment(memberCouponId, paymentId);
    }
}
