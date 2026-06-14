package org.personal.project.entity.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.personal.project.entity.Order;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_order_coupon", indexes = {
        @Index(name = "idx_order_coupon_member_coupon", columnList = "member_coupon_id"),
        @Index(name = "idx_order_coupon_status", columnList = "status")
}, uniqueConstraints = {
        // 결제 한 번에 쿠폰 한 개만 적용
        @UniqueConstraint(name = "uk_order_coupon_order", columnNames = "order_id")
})
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_coupon_id")
    private Long orderCouponId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_coupon_id", nullable = false)
    private MemberCoupon memberCoupon;

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderCouponStatus status = OrderCouponStatus.RESERVED;

    @CreationTimestamp
    @Column(name = "reserved_at", updatable = false)
    private LocalDateTime reservedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "restored_at")
    private LocalDateTime restoredAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
