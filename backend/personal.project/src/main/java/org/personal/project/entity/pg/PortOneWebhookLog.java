package org.personal.project.entity.pg;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_portone_webhook_log", indexes = {
        @Index(name = "idx_portone_webhook_payment", columnList = "payment_id")
})
@Getter
@NoArgsConstructor
public class PortOneWebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column(name = "webhook_id", nullable = false, unique = true, length = 120)
    private String webhookId;

    @Column(name = "payment_id", length = 120)
    private String paymentId;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(length = 50)
    private String result;

    @Lob
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @CreationTimestamp
    @Column(name = "processed_at", updatable = false)
    private LocalDateTime processedAt;

    public static PortOneWebhookLog create(
            String webhookId,
            String paymentId,
            String eventType,
            String result,
            String payload
    ) {
        PortOneWebhookLog log = new PortOneWebhookLog();
        log.webhookId = webhookId;
        log.paymentId = paymentId;
        log.eventType = eventType;
        log.result = result;
        log.payload = payload;
        return log;
    }
}
