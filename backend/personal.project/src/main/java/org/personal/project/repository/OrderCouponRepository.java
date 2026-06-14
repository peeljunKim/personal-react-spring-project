package org.personal.project.repository;

import org.personal.project.entity.coupon.OrderCoupon;
import org.personal.project.entity.coupon.OrderCouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderCouponRepository extends JpaRepository<OrderCoupon, Long> {

    Optional<OrderCoupon> findByOrderOno(Long orderId);

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
