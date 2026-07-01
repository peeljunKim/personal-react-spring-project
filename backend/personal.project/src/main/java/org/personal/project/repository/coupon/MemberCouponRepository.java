package org.personal.project.repository.coupon;

import org.personal.project.entity.coupon.MemberCoupon;
import org.personal.project.entity.coupon.MemberCouponStatus;
import org.personal.project.entity.coupon.CouponPolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    /**
     * 사용자별 발급 여부 조회
     * <p>중복 발급 응답 처리 또는 발급 전 사전 확인용</p>
     */
    Optional<MemberCoupon> findByPolicyPolicyIdAndMemberEmail(Long policyId, String memberId);

    /**
     * 내 쿠폰함 전체 조회
     */
    @EntityGraph(attributePaths = {"policy"})
    Page<MemberCoupon> findByMemberEmail(String memberId, Pageable pageable);

    /**
     * 내 쿠폰함 상태별 조회
     */
    @EntityGraph(attributePaths = {"policy"})
    Page<MemberCoupon> findByMemberEmailAndStatusIn(
            String memberId,
            Collection<MemberCouponStatus> statuses,
            Pageable pageable
    );

    /**
     * 사용 가능 쿠폰 조회
     */
    @EntityGraph(attributePaths = {"policy"})
    @Query("""
            select c
              from MemberCoupon c
            where c.member.email = :memberId
               and c.status = :issuedStatus
               and c.policy.status in :policyStatuses
               and c.policy.useStartAt <= :now
               and c.policy.useEndAt > :now
             order by c.policy.useEndAt asc, c.issuedAt desc
            """)
    List<MemberCoupon> findUsableCoupons(
            @Param("memberId") String memberId,
            @Param("issuedStatus") MemberCouponStatus issuedStatus,
            @Param("policyStatuses") Collection<CouponPolicyStatus> policyStatuses,
            @Param("now") LocalDateTime now
    );

    /**
     * 사용자 쿠폰 단건 조회
     */
    @EntityGraph(attributePaths = {"policy"})
    Optional<MemberCoupon> findByMemberCouponIdAndMemberEmail(Long memberCouponId, String memberId);

    /**
     * 관리자 사용자 쿠폰 조회
     * <p>
     * 단 파라미터에 null 값이 오면 해당 조건 제외
     */
    @EntityGraph(attributePaths = {"policy"})
    @Query("""
            select c
              from MemberCoupon c
             where (:memberId is null or c.member.email = :memberId)
               and (:status is null or c.status = :status)
               and (:policyId is null or c.policy.policyId = :policyId)
            """)
    Page<MemberCoupon> findAdminMemberCoupons(
            @Param("memberId") String memberId,
            @Param("status") MemberCouponStatus status,
            @Param("policyId") Long policyId,
            Pageable pageable
    );

    /**
     * 정책별 예약 쿠폰 존재 여부
     */
    boolean existsByPolicyPolicyIdAndStatus(Long policyId, MemberCouponStatus status);

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
     * 쿠폰 사용 확정
     * <p>Redis 임시 예약 검증 후 ISSUED 상태를 USED로 변경</p>
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update MemberCoupon c
               set c.status = :usedStatus,
                   c.usedAt = :now,
                   c.version = c.version + 1
             where c.memberCouponId = :memberCouponId
               and c.member.email = :memberId
               and c.status = :issuedStatus
            """)
    int confirmUseIfIssued(
            @Param("memberCouponId") Long memberCouponId,
            @Param("memberId") String memberId,
            @Param("issuedStatus") MemberCouponStatus issuedStatus,
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

    /**
     * 만료 쿠폰 일괄 변경
     * <p>사용 기간이 지난 ISSUED 쿠폰만 EXPIRED로 변경</p>
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update MemberCoupon c
               set c.status = :expiredStatus,
                   c.expiredAt = :now,
                   c.version = c.version + 1
             where c.status = :issuedStatus
               and c.policy.policyId in (
                   select p.policyId
                     from CouponPolicy p
                    where p.useEndAt <= :now
                      and p.status in :eligiblePolicyStatuses
               )
            """)
    int expireIssuedCoupons(
            @Param("issuedStatus") MemberCouponStatus issuedStatus,
            @Param("expiredStatus") MemberCouponStatus expiredStatus,
            @Param("eligiblePolicyStatuses") Collection<CouponPolicyStatus> eligiblePolicyStatuses,
            @Param("now") LocalDateTime now
    );

    /**
     * 발급된 쿠폰 일괄 회수
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update MemberCoupon c
               set c.status = :canceledStatus,
                   c.canceledAt = :now,
                   c.version = c.version + 1
             where c.policy.policyId = :policyId
               and c.status = :issuedStatus
            """)
    int cancelIssuedCouponsByPolicyId(
            @Param("policyId") Long policyId,
            @Param("issuedStatus") MemberCouponStatus issuedStatus,
            @Param("canceledStatus") MemberCouponStatus canceledStatus,
            @Param("now") LocalDateTime now
    );
}
