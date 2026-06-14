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

    /**
     * Outbox 조회
     * <p>requestKey 기준 중복 발행 확인용</p>
     */
    Optional<CouponIssueOutbox> findByRequestKey(String requestKey);

    /**
     * 발행 대상 Outbox 조회
     * <p>publisher polling 작업에서 사용</p>
     */
    List<CouponIssueOutbox> findByStatusInOrderByCreatedAtAsc(
            Collection<CouponIssueOutboxStatus> statuses,
            Pageable pageable
    );

    /**
     * 메시지 발행 시작
     * <p>PENDING/재시도 대상만 PUBLISHING으로 선점</p>
     */
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

    /**
     * 메시지 발행 성공 처리
     * <p>RabbitMQ publisher confirm 성공 시 사용</p>
     */
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

    /**
     * 발행 결과 불명확 처리
     * <p>confirm timeout 시 즉시 보상하지 않기 위한 상태</p>
     */
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

    /**
     * Outbox 최종 실패 처리
     * <p>명확한 발행 실패 또는 재처리 한계 도달 시 사용</p>
     */
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
