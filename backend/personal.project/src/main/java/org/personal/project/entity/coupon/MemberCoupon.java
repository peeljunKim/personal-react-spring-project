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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.personal.project.entity.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_member_coupon", indexes = {
        @Index(name = "idx_member_coupon_member_status", columnList = "member_id, status"),
        @Index(name = "idx_member_coupon_policy_status", columnList = "policy_id, status"),
        @Index(name = "idx_member_coupon_reserved_order", columnList = "reserved_order_id")
}, uniqueConstraints = {
        // 사용자별 1회 발급 쿠폰 중복 발급 방지
        @UniqueConstraint(name = "uk_member_coupon_policy_member", columnNames = {"policy_id", "member_id"})
})
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_coupon_id")
    private Long memberCouponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private CouponPolicy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberCouponStatus status = MemberCouponStatus.ISSUED;

    @Column(name = "reserved_order_id")
    private Long reservedOrderId;

    @CreationTimestamp
    @Column(name = "issued_at", updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Version
    private Long version;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
