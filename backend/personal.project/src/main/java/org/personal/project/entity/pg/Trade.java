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

    private static final int FAILURE_REASON_MAX_LENGTH = 255;

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

    @Column(name = "provider_status", length = 50)
    private String providerStatus; // PG 원본 결제 상태

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt; // PG 상태를 마지막으로 확인한 시각

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
    public void approve(String providerStatus) {
        if (this.status == TradeStatus.APPROVED) {
            throw new IllegalStateException("이미 승인된 거래입니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        this.status = TradeStatus.APPROVED;
        this.approvedAt = now;
        this.providerStatus = providerStatus;
        this.failureReason = null;
        this.verifiedAt = now;
    }

    /**
     * 승인 실패
     */
    public void fail(String providerStatus, String failureReason) {
        if (this.status == TradeStatus.APPROVED) {
            throw new IllegalStateException("이미 승인된 거래는 실패 처리할 수 없습니다.");
        }

        this.status = TradeStatus.FAILED;
        this.providerStatus = providerStatus;
        this.failureReason = truncateFailureReason(failureReason);
        this.verifiedAt = LocalDateTime.now();
    }

    public void markProviderPending(String providerStatus) {
        if (this.status != TradeStatus.READY) {
            return;
        }
        this.providerStatus = providerStatus;
        this.verifiedAt = LocalDateTime.now();
    }

    private String truncateFailureReason(String failureReason) {
        if (failureReason == null || failureReason.length() <= FAILURE_REASON_MAX_LENGTH) {
            return failureReason;
        }
        return failureReason.substring(0, FAILURE_REASON_MAX_LENGTH);
    }
}
