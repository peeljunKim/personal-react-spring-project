package org.personal.project.entity.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_coupon_issue_outbox", indexes = {
        @Index(name = "idx_coupon_issue_outbox_status_created", columnList = "status, created_at"),
        @Index(name = "idx_coupon_issue_outbox_status_last_tried", columnList = "status, last_tried_at")
}, uniqueConstraints = {
        // 동일 요청의 RabbitMQ 메시지 중복 발행 방지
        @UniqueConstraint(name = "uk_coupon_issue_outbox_request_key", columnNames = "request_key")
})
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponIssueOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_id")
    private Long outboxId;

    @Column(name = "request_key", nullable = false, length = 120)
    private String requestKey;

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(name = "member_id", nullable = false, length = 100)
    private String memberId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CouponIssueOutboxStatus status = CouponIssueOutboxStatus.PENDING;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "last_tried_at")
    private LocalDateTime lastTriedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
