package org.personal.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_order", indexes = {
        @Index(name = "idx_order_payment_id", columnList = "payment_id")
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

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // READY, PAID, CANCEL

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false) // email이 member에 pk임
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public static Order ready(Member member, Integer amount) {
        Order order = new Order();
        order.member = member;
        order.amount = amount;
        order.status = OrderStatus.READY;
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

    public void markPaid() {
        if (this.status == OrderStatus.PAID) {
            return;
        }
        if (this.status == OrderStatus.CANCEL) {
            throw new IllegalStateException("취소된 주문은 결제 완료 처리할 수 없습니다.");
        }
        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void markCancelled() {
        if (this.status == OrderStatus.CANCEL) {
            return;
        }
        this.status = OrderStatus.CANCEL;
        this.cancelledAt = LocalDateTime.now();
    }
}
