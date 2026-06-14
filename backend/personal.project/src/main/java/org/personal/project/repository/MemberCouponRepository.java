package org.personal.project.repository;

import org.personal.project.entity.coupon.MemberCoupon;
import org.personal.project.entity.coupon.MemberCouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    Optional<MemberCoupon> findByPolicyPolicyIdAndMemberEmail(Long policyId, String memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update MemberCoupon c
               set c.status = :reservedStatus,
                   c.reservedOrderId = :orderId,
                   c.reservedAt = :now,
                   c.version = c.version + 1
             where c.memberCouponId = :memberCouponId
               and c.member.email = :memberId
               and c.status = :issuedStatus
            """)
    int reserveIfIssued(
            @Param("memberCouponId") Long memberCouponId,
            @Param("memberId") String memberId,
            @Param("orderId") Long orderId,
            @Param("issuedStatus") MemberCouponStatus issuedStatus,
            @Param("reservedStatus") MemberCouponStatus reservedStatus,
            @Param("now") LocalDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update MemberCoupon c
               set c.status = :usedStatus,
                   c.usedAt = :now,
                   c.version = c.version + 1
             where c.memberCouponId = :memberCouponId
               and c.reservedOrderId = :orderId
               and c.status = :reservedStatus
            """)
    int confirmUseIfReserved(
            @Param("memberCouponId") Long memberCouponId,
            @Param("orderId") Long orderId,
            @Param("reservedStatus") MemberCouponStatus reservedStatus,
            @Param("usedStatus") MemberCouponStatus usedStatus,
            @Param("now") LocalDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update MemberCoupon c
               set c.status = :issuedStatus,
                   c.reservedOrderId = null,
                   c.reservedAt = null,
                   c.version = c.version + 1
             where c.memberCouponId = :memberCouponId
               and c.reservedOrderId = :orderId
               and c.status = :reservedStatus
            """)
    int releaseReservationIfReserved(
            @Param("memberCouponId") Long memberCouponId,
            @Param("orderId") Long orderId,
            @Param("reservedStatus") MemberCouponStatus reservedStatus,
            @Param("issuedStatus") MemberCouponStatus issuedStatus
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update MemberCoupon c
               set c.status = :nextStatus,
                   c.reservedOrderId = null,
                   c.reservedAt = null,
                   c.expiredAt = case when :nextStatus = :expiredStatus then :now else c.expiredAt end,
                   c.canceledAt = case when :nextStatus = :canceledStatus then :now else c.canceledAt end,
                   c.version = c.version + 1
             where c.memberCouponId = :memberCouponId
               and c.status = :usedStatus
            """)
    int restoreAfterRefundIfUsed(
            @Param("memberCouponId") Long memberCouponId,
            @Param("usedStatus") MemberCouponStatus usedStatus,
            @Param("nextStatus") MemberCouponStatus nextStatus,
            @Param("expiredStatus") MemberCouponStatus expiredStatus,
            @Param("canceledStatus") MemberCouponStatus canceledStatus,
            @Param("now") LocalDateTime now
    );
}
