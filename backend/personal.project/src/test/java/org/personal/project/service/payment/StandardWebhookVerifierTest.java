package org.personal.project.service.payment;

import org.junit.jupiter.api.Test;
import org.personal.project.properties.PortOnePaymentProperties;
import org.springframework.http.HttpHeaders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StandardWebhookVerifierTest {

    private static final byte[] SECRET = "test-secret-key-test-secret-key".getBytes(StandardCharsets.UTF_8);
    private static final Instant NOW = Instant.parse("2026-05-07T06:00:00Z");

    @Test
    void verifiesStandardWebhookSignature() throws Exception {
        StandardWebhookVerifier verifier = verifier();
        String body = "{\"type\":\"Transaction.Paid\",\"data\":{\"paymentId\":\"payment-1\"}}";
        HttpHeaders headers = signedHeaders("msg_1", NOW.getEpochSecond(), body);

        assertDoesNotThrow(() -> verifier.verify(body, headers));
    }

    @Test
    void rejectsTamperedBody() throws Exception {
        StandardWebhookVerifier verifier = verifier();
        String body = "{\"type\":\"Transaction.Paid\",\"data\":{\"paymentId\":\"payment-1\"}}";
        HttpHeaders headers = signedHeaders("msg_1", NOW.getEpochSecond(), body);

        assertThrows(PaymentException.class, () -> verifier.verify(body + " ", headers));
    }

    @Test
    void rejectsOldTimestamp() throws Exception {
        StandardWebhookVerifier verifier = verifier();
        String body = "{\"type\":\"Transaction.Paid\",\"data\":{\"paymentId\":\"payment-1\"}}";
        HttpHeaders headers = signedHeaders("msg_1", NOW.minusSeconds(600).getEpochSecond(), body);

        assertThrows(PaymentException.class, () -> verifier.verify(body, headers));
    }

    private StandardWebhookVerifier verifier() {
        PortOnePaymentProperties properties = new PortOnePaymentProperties();
        properties.setWebhookSecret("whsec_" + Base64.getEncoder().encodeToString(SECRET));
        return new StandardWebhookVerifier(properties, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private HttpHeaders signedHeaders(String webhookId, long timestamp, String body) throws Exception {
        String content = webhookId + "." + timestamp + "." + body;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));

        HttpHeaders headers = new HttpHeaders();
        headers.add("webhook-id", webhookId);
        headers.add("webhook-timestamp", String.valueOf(timestamp));
        headers.add("webhook-signature", "v1," + Base64.getEncoder().encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8))));
        return headers;
    }
}
