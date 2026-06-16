package org.personal.project.entity.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_coupon_issue_history", indexes = {
        @Index(name = "idx_coupon_issue_history_policy", columnList = "policy_id"),
        @Index(name = "idx_coupon_issue_history_member", columnList = "member_id"),
        @Index(name = "idx_coupon_issue_history_request_key", columnList = "request_key")
})
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponIssueHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(name = "policy_name", nullable = false, length = 255)
    private String policyName;

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;

    @Column(name = "member_id", nullable = false, length = 255)
    private String memberId;

    @Column(name = "request_key", length = 120)
    private String requestKey;

    @Column(nullable = false, length = 30)
    private String result;

    @Column(length = 255)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
