package org.personal.project.entity.pg;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_webhook_log")
@Getter
@NoArgsConstructor
public class WebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    private Trade trade; // 관련 거래 ID

    @Column(length = 100)
    private String result;

    @Column(name = "sent_at")
    private LocalDateTime sentAt; // 콜백 전송 시간

    /**
     * 생성 메소드
     */
    public static WebhookLog create(Trade trade, String result) {
        WebhookLog log = new WebhookLog();
        log.trade = trade;
        log.result = result;
//        log.sentAt = LocalDateTime.now();
        return log;
    }
}