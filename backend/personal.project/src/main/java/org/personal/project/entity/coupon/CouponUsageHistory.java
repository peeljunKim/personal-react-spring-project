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
@Table(name = "tbl_coupon_usage_history", indexes = {
        @Index(name = "idx_coupon_usage_history_member_coupon", columnList = "member_coupon_id"),
        @Index(name = "idx_coupon_usage_history_order", columnList = "order_id"),
        @Index(name = "idx_coupon_usage_history_event", columnList = "event_type")
})
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponUsageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "member_coupon_id", nullable = false)
    private Long memberCouponId;

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(name = "policy_name", nullable = false, length = 255)
    private String policyName;

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;

    @Column(name = "member_id", nullable = false, length = 255)
    private String memberId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "payment_id", length = 100)
    private String paymentId;

    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    @Column
    private Integer amount;

    @Column(length = 255)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
