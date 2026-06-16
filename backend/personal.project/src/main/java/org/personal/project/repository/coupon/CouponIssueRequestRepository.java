package org.personal.project.repository.coupon;

import org.personal.project.entity.coupon.CouponIssueRequest;
import org.personal.project.entity.coupon.CouponIssueRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponIssueRequestRepository extends JpaRepository<CouponIssueRequest, Long> {

    /**
     * 발급 요청 조회
     * <p>requestKey 기준 멱등 응답 반환용</p>
     */
    Optional<CouponIssueRequest> findByRequestKey(String requestKey);

    /**
     * 발급 요청 처리 시작
     * <p>PENDING 요청만 PROCESSING으로 변경</p>
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CouponIssueRequest r
               set r.status = :processingStatus,
                   r.startedAt = :now
             where r.requestKey = :requestKey
               and r.status = :pendingStatus
            """)
    int markProcessingIfPending(
            @Param("requestKey") String requestKey,
            @Param("pendingStatus") CouponIssueRequestStatus pendingStatus,
            @Param("processingStatus") CouponIssueRequestStatus processingStatus,
            @Param("now") LocalDateTime now
    );

    /**
     * 발급 성공 처리
     * <p>MemberCoupon 생성 완료 후 사용자 결과 상태 변경</p>
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CouponIssueRequest r
               set r.status = :succeededStatus,
                   r.failureReason = null,
                   r.completedAt = :now
             where r.requestKey = :requestKey
               and r.status = :processingStatus
            """)
    int markSucceededIfProcessing(
            @Param("requestKey") String requestKey,
            @Param("processingStatus") CouponIssueRequestStatus processingStatus,
            @Param("succeededStatus") CouponIssueRequestStatus succeededStatus,
            @Param("now") LocalDateTime now
    );

    /**
     * 발급 실패 처리
     * <p>수량 초과, 중복 발급, Consumer 실패 결과 저장</p>
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CouponIssueRequest r
               set r.status = :failedStatus,
                   r.failureReason = :failureReason,
                   r.completedAt = :now
             where r.requestKey = :requestKey
               and r.status in :updatableStatuses
            """)
    int markFailedIfNotCompleted(
            @Param("requestKey") String requestKey,
            @Param("updatableStatuses") Iterable<CouponIssueRequestStatus> updatableStatuses,
            @Param("failedStatus") CouponIssueRequestStatus failedStatus,
            @Param("failureReason") String failureReason,
            @Param("now") LocalDateTime now
    );
}
