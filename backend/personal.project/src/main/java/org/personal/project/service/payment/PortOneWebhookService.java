package org.personal.project.service.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.dto.payment.PaymentSyncResponse;
import org.personal.project.entity.pg.PortOneWebhookLog;
import org.personal.project.exception.PaymentException;
import org.personal.project.repository.PortOneWebhookLogRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortOneWebhookService {

    private final StandardWebhookVerifier webhookVerifier;
    private final WebhookIdempotencyService idempotencyService;
    private final PaymentSynchronizerService paymentSynchronizerService;
    private final PortOneWebhookLogRepository webhookLogRepository;
    private final ObjectMapper objectMapper;

    public PaymentSyncResponse handle(String rawBody, HttpHeaders headers) {
        webhookVerifier.verify(rawBody, headers);

        String webhookId = headers.getFirst("webhook-id");
        if (!StringUtils.hasText(webhookId)) {
            throw new PaymentException("포트원 웹훅 ID가 누락되었습니다.");
        }

        WebhookPayload payload = parse(rawBody);
        if (!StringUtils.hasText(payload.paymentId())) {
            log.info("결제 이벤트가 아닌 포트원 웹훅은 무시합니다. webhookId={}, type={}", webhookId, payload.type());
            saveLog(webhookId, null, payload.type(), "IGNORED", rawBody);
            return PaymentSyncResponse.builder()
                    .paymentStatus("IGNORED")
                    .message("결제 이벤트가 아닌 웹훅입니다.")
                    .build();
        }

        if (!idempotencyService.tryStart(webhookId)) {
            log.info("중복 포트원 웹훅을 멱등 처리합니다. webhookId={}, paymentId={}", webhookId, payload.paymentId());
            return PaymentSyncResponse.builder()
                    .paymentId(payload.paymentId())
                    .paymentStatus("DUPLICATE")
                    .message("이미 처리한 웹훅입니다.")
                    .build();
        }

        try {
            PaymentSyncResponse response = paymentSynchronizerService.synchronize(payload.paymentId());
            saveLog(webhookId, payload.paymentId(), payload.type(), response.orderStatus(), rawBody);
            idempotencyService.markProcessed(webhookId);
            return response;
        } catch (RuntimeException e) {
            idempotencyService.release(webhookId);
            throw e;
        }
    }

    private WebhookPayload parse(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String type = root.path("type").asText(null);
            String paymentId = root.path("data").path("paymentId").asText(null);
            return new WebhookPayload(type, paymentId);
        } catch (IOException e) {
            throw new PaymentException("포트원 웹훅 JSON 파싱에 실패했습니다.", e);
        }
    }

    private void saveLog(String webhookId, String paymentId, String type, String result, String rawBody) {
        if (webhookLogRepository.existsByWebhookId(webhookId)) {
            return;
        }
        webhookLogRepository.save(PortOneWebhookLog.create(webhookId, paymentId, type, result, rawBody));
    }

    private record WebhookPayload(String type, String paymentId) {
    }
}
