package org.personal.project.entity.pg;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 결제 요청 테이블
 */
@Entity
@Table(name = "tbl_payment_request")
@Getter
@NoArgsConstructor
public class PaymentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;       // 가맹점 주문 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;  // 가맹정 id

    @Column(nullable = false)
    private Integer amount;     // 결제 요청 금액

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "method_id", nullable = false)
    private PaymentMethod paymentMethod;  // 결제 수단

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;    // 요청 생성 날짜

    /**
     * 생성 메서드
     */
    public static PaymentRequest create(
            Long orderId,
            Merchant merchant,
            Integer amount,
            PaymentMethod paymentMethod
    ) {
        PaymentRequest pr = new PaymentRequest();
        pr.orderId = orderId;
        pr.merchant = merchant;
        pr.amount = amount;
        pr.paymentMethod = paymentMethod;
//        pr.createdAt = LocalDateTime.now();
        return pr;
    }
}