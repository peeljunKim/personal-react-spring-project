package org.personal.project.service.payment;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.properties.PortOnePaymentProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortOnePaymentClient {

    private final RestClient portOneRestClient;
    private final PortOnePaymentProperties properties;

    public PortOnePayment getPayment(String paymentId) {
        assertApiSecretConfigured();

        JsonNode root = portOneRestClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .retrieve()
                .body(JsonNode.class);

        JsonNode payment = unwrapPayment(root);
        String status = payment.path("status").asText();
        Integer totalAmount = payment.path("amount").path("total").isMissingNode()
                ? null
                : payment.path("amount").path("total").asInt();
        String transactionId = payment.path("transactionId").isMissingNode()
                ? null
                : payment.path("transactionId").asText(null);
        String id = payment.path("id").asText(paymentId);

        return new PortOnePayment(id, status, totalAmount, transactionId);
    }

    public void cancelPayment(String paymentId, String reason) {
        assertApiSecretConfigured();

        Map<String, Object> body = new LinkedHashMap<>();
        if (StringUtils.hasText(properties.getStoreId())) {
            body.put("storeId", properties.getStoreId());
        }
        body.put("reason", reason);

        portOneRestClient.post()
                .uri("/payments/{paymentId}/cancel", paymentId)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        log.warn("PortOne 결제 취소 요청 완료. paymentId={}, reason={}", paymentId, reason);
    }

    private JsonNode unwrapPayment(JsonNode root) {
        if (root == null) {
            throw new PaymentException("포트원 결제 조회 응답이 비어 있습니다.");
        }
        if (root.has("payment")) {
            return root.get("payment");
        }
        return root;
    }

    private void assertApiSecretConfigured() {
        if (!StringUtils.hasText(properties.getApiSecret())) {
            throw new PaymentException("PORTONE_API_SECRET 환경 변수가 설정되어 있지 않습니다.");
        }
    }
}
