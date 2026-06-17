package org.personal.project.entity.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_coupon_policy", indexes = {
        @Index(name = "idx_coupon_policy_status", columnList = "status"),
        @Index(name = "idx_coupon_policy_issue_type", columnList = "issue_type"),
        @Index(name = "idx_coupon_policy_issue_period", columnList = "issue_start_at, issue_end_at"),
        @Index(name = "idx_coupon_policy_use_period", columnList = "use_start_at, use_end_at")
})
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long policyId;

    @Column(nullable = false, length = 120)
    private String name;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false, length = 30)
    private CouponIssueType issueType = CouponIssueType.GENERAL;

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;

    @Column(name = "min_order_amount", nullable = false)
    private Integer minOrderAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "apply_scope", nullable = false, length = 20)
    private CouponApplyScope applyScope;

    @Column(name = "total_issue_limit", nullable = false)
    private Integer totalIssueLimit;

    @Builder.Default
    @Column(name = "issued_count", nullable = false)
    private Integer issuedCount = 0;

    @Builder.Default
    @Column(name = "per_member_issue_limit", nullable = false)
    private Integer perMemberIssueLimit = 1;

    @Builder.Default
    @Column(name = "per_member_use_limit", nullable = false)
    private Integer perMemberUseLimit = 1;

    @Column(name = "issue_start_at", nullable = false)
    private LocalDateTime issueStartAt;

    @Column(name = "issue_end_at", nullable = false)
    private LocalDateTime issueEndAt;

    @Column(name = "use_start_at", nullable = false)
    private LocalDateTime useStartAt;

    @Column(name = "use_end_at", nullable = false)
    private LocalDateTime useEndAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CouponPolicyStatus status = CouponPolicyStatus.DRAFT;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
