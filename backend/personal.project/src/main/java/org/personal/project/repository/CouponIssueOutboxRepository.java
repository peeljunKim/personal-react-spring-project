package org.personal.project.repository;

import org.personal.project.entity.coupon.CouponIssueOutbox;
import org.personal.project.entity.coupon.CouponIssueOutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CouponIssueOutboxRepository extends JpaRepository<CouponIssueOutbox, Long> {

    Optional<CouponIssueOutbox> findByRequestKey(String requestKey);

    List<CouponIssueOutbox> findByStatusInOrderByCreatedAtAsc(
            Collection<CouponIssueOutboxStatus> statuses,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CouponIssueOutbox o
               set o.status = :publishingStatus,
                   o.lastTriedAt = :now,
                   o.retryCount = o.retryCount + 1
             where o.outboxId = :outboxId
               and o.status in :publishableStatuses
            """)
    int markPublishingIfPublishable(
            @Param("outboxId") Long outboxId,
            @Param("publishableStatuses") Iterable<CouponIssueOutboxStatus> publishableStatuses,
            @Param("publishingStatus") CouponIssueOutboxStatus publishingStatus,
            @Param("now") LocalDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CouponIssueOutbox o
               set o.status = :publishedStatus,
                   o.publishedAt = :now,
                   o.failureReason = null
             where o.requestKey = :requestKey
               and o.status = :publishingStatus
            """)
    int markPublishedIfPublishing(
            @Param("requestKey") String requestKey,
            @Param("publishingStatus") CouponIssueOutboxStatus publishingStatus,
            @Param("publishedStatus") CouponIssueOutboxStatus publishedStatus,
            @Param("now") LocalDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CouponIssueOutbox o
               set o.status = :confirmUnknownStatus,
                   o.failureReason = :reason
             where o.requestKey = :requestKey
               and o.status = :publishingStatus
            """)
    int markConfirmUnknownIfPublishing(
            @Param("requestKey") String requestKey,
            @Param("publishingStatus") CouponIssueOutboxStatus publishingStatus,
            @Param("confirmUnknownStatus") CouponIssueOutboxStatus confirmUnknownStatus,
            @Param("reason") String reason
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CouponIssueOutbox o
               set o.status = :failedStatus,
                   o.failureReason = :reason
             where o.requestKey = :requestKey
               and o.status in :updatableStatuses
            """)
    int markFailedIfUpdatable(
            @Param("requestKey") String requestKey,
            @Param("updatableStatuses") Iterable<CouponIssueOutboxStatus> updatableStatuses,
            @Param("failedStatus") CouponIssueOutboxStatus failedStatus,
            @Param("reason") String reason
    );
}
