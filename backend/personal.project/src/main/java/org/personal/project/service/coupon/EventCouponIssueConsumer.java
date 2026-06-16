package org.personal.project.service.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.dto.coupon.CouponIssueMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 이벤트 쿠폰 발급 메시지 수신
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventCouponIssueConsumer {

    private final ObjectMapper objectMapper;
    private final EventCouponIssueProcessor eventCouponIssueProcessor;

    /**
     * RabbitMQ 메시지 수신 및 ACK/NACK 처리
     */
    @RabbitListener(
            queues = "${coupon.issue.rabbitmq.queue}",
            containerFactory = "couponIssueRabbitListenerContainerFactory"
    )
    public void consume(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            CouponIssueMessage issueMessage = objectMapper.readValue(message.getBody(), CouponIssueMessage.class);
            log.info("이벤트 쿠폰 메시지 수신 requestKey={}, policyId={}, memberId={}, deliveryTag={}",
                    issueMessage.requestKey(), issueMessage.policyId(), issueMessage.memberId(), deliveryTag);
            eventCouponIssueProcessor.process(issueMessage);
            channel.basicAck(deliveryTag, false);
            log.info("이벤트 쿠폰 메시지 ACK requestKey={}, deliveryTag={}",
                    issueMessage.requestKey(), deliveryTag);
        } catch (Exception e) {
            log.error("이벤트 쿠폰 메시지 처리 실패 deliveryTag={}", deliveryTag, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
