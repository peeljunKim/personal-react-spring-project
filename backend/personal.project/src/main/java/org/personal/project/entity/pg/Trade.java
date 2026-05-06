package org.personal.project.entity.pg;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 거래 처리 테이블
 */
@Entity
@Table(name = "tbl_trade")
@Getter
@NoArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long tradeId;

    @Column(length = 100, unique = true, nullable = false)
    private String tid;     // 거래 TID (Transaction ID)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private PaymentRequest paymentRequest;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TradeStatus status;  // 거래 상태 (READY, APPROVED, FAILED)

    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt; // 승인 요청 시간 (카드사로 요청 보낸 시점)

    @Column(name = "approved_at")
    private LocalDateTime approvedAt; // 승인 완료 시간 (응답 받은 시점)

    /**
     * 생성 메서드 (정적 팩토리)
     */
    public static Trade create(PaymentRequest request, String tid) {
        Trade trade = new Trade();
        trade.paymentRequest = request;
        trade.tid = tid;
        trade.status = TradeStatus.READY;
        return trade;
    }

    /**
     * PG 요청 시 호출
     */
    public void markRequested() {
        if (this.status != TradeStatus.READY) {
            throw new IllegalStateException("이미 요청된 거래입니다.");
        }
        this.status = TradeStatus.READY;
        this.requestedAt = LocalDateTime.now();
    }

    /**
     * 승인 성공
     */
    public void approve() {
        if (this.status == TradeStatus.APPROVED) {
            throw new IllegalStateException("이미 승인된 거래입니다.");
        }

        this.status = TradeStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * 승인 실패
     */
    public void fail() {
        if (this.status == TradeStatus.APPROVED) {
            throw new IllegalStateException("이미 승인된 거래는 실패 처리할 수 없습니다.");
        }

        this.status = TradeStatus.FAILED;
    }
}