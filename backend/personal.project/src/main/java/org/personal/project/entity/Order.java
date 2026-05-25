package org.personal.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_order", indexes = {
        @Index(name = "idx_order_payment_id", columnList = "payment_id"),
        @Index(name = "idx_order_archive_candidate", columnList = "status, pay_method, created_at, ono"),
        @Index(name = "idx_order_member_created", columnList = "member_id, created_at, ono"),
        @Index(name = "idx_order_payment_status_created", columnList = "payment_status, created_at, ono")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ono;
    private Integer amount; // 주문 금액

    @Column(name = "payment_id", unique = true, length = 100)
    private String paymentId;

    @Column(name = "pay_method", length = 50)
    private String payMethod;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // READY, PAID, CANCEL

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20, nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false) // email이 member에 pk임
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public static Order ready(Member member, Integer amount) {
        return ready(member, amount, null);
    }

    public static Order ready(Member member, Integer amount, String payMethod) {
        Order order = new Order();
        order.member = member;
        order.amount = amount;
        order.payMethod = payMethod;
        order.status = OrderStatus.READY;
        order.paymentStatus = PaymentStatus.READY;
        return order;
    }

    public void assignPaymentId(String paymentId) {
        if (this.paymentId != null) {
            throw new IllegalStateException("이미 결제 ID가 지정된 주문입니다.");
        }
        this.paymentId = paymentId;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
        item.assignOrder(this);
    }

    @PrePersist
    void defaultPaymentStatus() {
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.READY;
        }
    }

    public void markPaid() {
        if (this.status == OrderStatus.PAID) {
            return;
        }
        if (this.status == OrderStatus.CANCEL) {
            throw new IllegalStateException("취소된 주문은 결제 완료 처리할 수 없습니다.");
        }
        LocalDateTime now = LocalDateTime.now();
        this.status = OrderStatus.PAID;
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.paidAt = now;
    }

    public void markCancelled() {
        if (this.status == OrderStatus.CANCEL) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        this.status = OrderStatus.CANCEL;
        this.paymentStatus = PaymentStatus.CANCELED;
        this.cancelledAt = now;
    }

    public void markPaymentPending() {
        if (this.status == OrderStatus.PAID || this.status == OrderStatus.CANCEL) {
            return;
        }
        this.paymentStatus = PaymentStatus.PENDING;
    }

    public void markPaymentFailed() {
        if (this.status == OrderStatus.CANCEL) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        this.status = OrderStatus.CANCEL;
        this.paymentStatus = PaymentStatus.FAILED;
        this.cancelledAt = now;
    }
}
