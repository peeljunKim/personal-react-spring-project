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

    /**
     * 사용자별 발급 여부 조회
     * <p>중복 발급 응답 처리 또는 발급 전 사전 확인용</p>
     */
    Optional<MemberCoupon> findByPolicyPolicyIdAndMemberEmail(Long policyId, String memberId);

    /**
     * 쿠폰 사용 예약
     * <p>ISSUED 상태인 쿠폰만 주문에 예약</p>
     */
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

    /**
     * 쿠폰 사용 확정
     * <p>결제 승인 후 RESERVED 상태를 USED로 변경</p>
     */
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

    /**
     * 쿠폰 예약 해제
     * <p>결제 실패/시간 초과 시 RESERVED 상태를 ISSUED로 복구</p>
     */
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

    /**
     * 환불 후 쿠폰 복구
     * <p>USED 상태 쿠폰을 정책/기간 기준 상태로 변경</p>
     */
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
