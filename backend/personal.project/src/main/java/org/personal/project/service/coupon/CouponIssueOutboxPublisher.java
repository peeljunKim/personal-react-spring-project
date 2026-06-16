package org.personal.project.service.coupon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.entity.coupon.CouponIssueOutbox;
import org.personal.project.entity.coupon.CouponIssueOutboxStatus;
import org.personal.project.properties.CouponIssueRabbitMqProperties;
import org.personal.project.repository.coupon.CouponIssueOutboxRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 쿠폰 발급 Outbox 발행
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CouponIssueOutboxPublisher {

    private static final List<CouponIssueOutboxStatus> PUBLISHABLE_STATUSES = List.of(
            CouponIssueOutboxStatus.PENDING,
            CouponIssueOutboxStatus.PUBLISH_FAILED
    );

    private final CouponIssueOutboxRepository couponIssueOutboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final CouponIssueRabbitMqProperties rabbitMqProperties;

    /**
     * 발행 대기 Outbox 일괄 처리
     */
    public int publishPending(int limit) {
        List<CouponIssueOutbox> outboxes = couponIssueOutboxRepository.findByStatusInOrderByCreatedAtAsc(
                PUBLISHABLE_STATUSES,
                PageRequest.of(0, limit)
        );

        if (!outboxes.isEmpty()) {
            log.info("쿠폰 Outbox 발행 대상 조회 count={}, limit={}", outboxes.size(), limit);
        }

        int published = 0;
        for (CouponIssueOutbox outbox : outboxes) {
            if (publish(outbox)) {
                published++;
            }
        }
        return published;
    }

    /**
     * 단일 Outbox 발행 및 confirm 처리
     */
    private boolean publish(CouponIssueOutbox outbox) {
        int marked = couponIssueOutboxRepository.markPublishingIfPublishable(
                outbox.getOutboxId(),
                PUBLISHABLE_STATUSES,
                CouponIssueOutboxStatus.PUBLISHING,
                LocalDateTime.now()
        );
        if (marked != 1) {
            log.warn("쿠폰 Outbox 발행 스킵 outboxId={}, requestKey={}, status={}",
                    outbox.getOutboxId(), outbox.getRequestKey(), outbox.getStatus());
            return false;
        }

        CorrelationData correlationData = new CorrelationData(outbox.getRequestKey());
        try {
            log.info("쿠폰 Outbox 발행 시작 outboxId={}, requestKey={}, exchange={}, routingKey={}",
                    outbox.getOutboxId(),
                    outbox.getRequestKey(),
                    rabbitMqProperties.getExchange(),
                    rabbitMqProperties.getRoutingKey());
            Message message = MessageBuilder.withBody(outbox.getPayload().getBytes(StandardCharsets.UTF_8))
                    .setContentType("application/json")
                    .build();

            rabbitTemplate.send(
                    rabbitMqProperties.getExchange(),
                    rabbitMqProperties.getRoutingKey(),
                    message,
                    correlationData
            );

            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(rabbitMqProperties.getConfirmTimeoutMillis(), TimeUnit.MILLISECONDS);
            if (confirm.isAck()) {
                couponIssueOutboxRepository.markPublishedIfPublishing(
                        outbox.getRequestKey(),
                        CouponIssueOutboxStatus.PUBLISHING,
                        CouponIssueOutboxStatus.PUBLISHED,
                        LocalDateTime.now()
                );
                log.info("쿠폰 Outbox 발행 성공 requestKey={}", outbox.getRequestKey());
                return true;
            }

            log.warn("쿠폰 Outbox 발행 NACK requestKey={}, reason={}",
                    outbox.getRequestKey(), confirm.getReason());
            couponIssueOutboxRepository.markFailedIfUpdatable(
                    outbox.getRequestKey(),
                    List.of(CouponIssueOutboxStatus.PUBLISHING),
                    CouponIssueOutboxStatus.PUBLISH_FAILED,
                    confirm.getReason()
            );
            return false;
        } catch (TimeoutException e) {
            log.warn("쿠폰 Outbox 발행 confirm timeout requestKey={}, timeoutMillis={}",
                    outbox.getRequestKey(), rabbitMqProperties.getConfirmTimeoutMillis());
            couponIssueOutboxRepository.markConfirmUnknownIfPublishing(
                    outbox.getRequestKey(),
                    CouponIssueOutboxStatus.PUBLISHING,
                    CouponIssueOutboxStatus.CONFIRM_UNKNOWN,
                    "PUBLISH_CONFIRM_TIMEOUT"
            );
            return false;
        } catch (Exception e) {
            log.error("쿠폰 Outbox 발행 예외 requestKey={}", outbox.getRequestKey(), e);
            couponIssueOutboxRepository.markFailedIfUpdatable(
                    outbox.getRequestKey(),
                    List.of(CouponIssueOutboxStatus.PUBLISHING),
                    CouponIssueOutboxStatus.PUBLISH_FAILED,
                    e.getMessage()
            );
            return false;
        }
    }
}
