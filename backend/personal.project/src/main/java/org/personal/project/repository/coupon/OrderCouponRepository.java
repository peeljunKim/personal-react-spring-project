package org.personal.project.repository.coupon;

import org.personal.project.entity.coupon.OrderCoupon;
import org.personal.project.entity.coupon.OrderCouponStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderCouponRepository extends JpaRepository<OrderCoupon, Long> {

    /**
     * 주문 쿠폰 조회
     * <p>연관 데이터가 필요 없는 단순 주문 쿠폰 조회 시 사용</p>
     */
    Optional<OrderCoupon> findByOrderOno(Long orderId);

    /**
     * 주문 쿠폰 이력용 조회
     * <p>주문, 사용자 쿠폰, 정책, 회원 정보 함께 조회 시 사용</p>
     */
    @EntityGraph(attributePaths = {"order", "memberCoupon", "memberCoupon.policy", "memberCoupon.member"})
    Optional<OrderCoupon> findForHistoryByOrderOno(Long orderId);

    /**
     * 주문 쿠폰 확정
     * <p>결제 승인 후 RESERVED 상태만 CONFIRMED로 변경</p>
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update OrderCoupon c
               set c.status = :confirmedStatus,
                   c.confirmedAt = :now
             where c.order.ono = :orderId
               and c.status = :reservedStatus
            """)
    int markConfirmedIfReserved(
            @Param("orderId") Long orderId,
            @Param("reservedStatus") OrderCouponStatus reservedStatus,
            @Param("confirmedStatus") OrderCouponStatus confirmedStatus,
            @Param("now") LocalDateTime now
    );

    /**
     * 주문 쿠폰 예약 해제
     * <p>결제 실패/시간 초과 시 RESERVED 상태만 RELEASED로 변경</p>
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update OrderCoupon c
               set c.status = :releasedStatus,
                   c.releasedAt = :now
             where c.order.ono = :orderId
               and c.status = :reservedStatus
            """)
    int markReleasedIfReserved(
            @Param("orderId") Long orderId,
            @Param("reservedStatus") OrderCouponStatus reservedStatus,
            @Param("releasedStatus") OrderCouponStatus releasedStatus,
            @Param("now") LocalDateTime now
    );

    /**
     * 주문 쿠폰 복구 완료 처리
     * <p>환불 시 CONFIRMED 상태만 RESTORED로 변경</p>
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update OrderCoupon c
               set c.status = :restoredStatus,
                   c.restoredAt = :now
             where c.order.ono = :orderId
               and c.status = :confirmedStatus
            """)
    int markRestoredIfConfirmed(
            @Param("orderId") Long orderId,
            @Param("confirmedStatus") OrderCouponStatus confirmedStatus,
            @Param("restoredStatus") OrderCouponStatus restoredStatus,
            @Param("now") LocalDateTime now
    );
}
