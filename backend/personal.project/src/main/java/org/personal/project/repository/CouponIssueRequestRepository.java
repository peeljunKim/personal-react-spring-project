package org.personal.project.repository;

import org.personal.project.entity.coupon.CouponIssueRequest;
import org.personal.project.entity.coupon.CouponIssueRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponIssueRequestRepository extends JpaRepository<CouponIssueRequest, Long> {

    Optional<CouponIssueRequest> findByRequestKey(String requestKey);

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
