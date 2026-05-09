package org.personal.project.service.payment;

import lombok.RequiredArgsConstructor;
import org.personal.project.properties.PortOnePaymentProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class StandardWebhookVerifier {

    private static final Duration TIMESTAMP_TOLERANCE = Duration.ofMinutes(5);
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SIGNATURE_PREFIX = "v1,";
    private static final String SECRET_PREFIX = "whsec_";

    private final PortOnePaymentProperties properties;
    private final Clock clock;

    public void verify(String rawBody, HttpHeaders headers) {
        if (!properties.isWebhookSignatureRequired()) {
            return;
        }

        String webhookId = headers.getFirst("webhook-id");
        String timestamp = headers.getFirst("webhook-timestamp");
        String signatureHeader = headers.getFirst("webhook-signature");

        if (!StringUtils.hasText(webhookId)
                || !StringUtils.hasText(timestamp)
                || !StringUtils.hasText(signatureHeader)
                || !StringUtils.hasText(properties.getWebhookSecret())) {
            throw new PaymentException("포트원 웹훅 검증 헤더 또는 시크릿이 누락되었습니다.");
        }

        assertTimestampInTolerance(timestamp);

        byte[] expected = hmac(webhookId + "." + timestamp + "." + rawBody);
        for (String signature : signatureHeader.split(" ")) {
            if (!signature.startsWith(SIGNATURE_PREFIX)) {
                continue;
            }
            byte[] actual = Base64.getDecoder().decode(signature.substring(SIGNATURE_PREFIX.length()));
            if (MessageDigest.isEqual(expected, actual)) {
                return;
            }
        }

        throw new PaymentException("포트원 웹훅 시그니처 검증에 실패했습니다.");
    }

    private void assertTimestampInTolerance(String timestamp) {
        long epochSecond;
        try {
            epochSecond = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new PaymentException("포트원 웹훅 timestamp 형식이 올바르지 않습니다.", e);
        }

        Instant receivedAt = Instant.ofEpochSecond(epochSecond);
        Duration diff = Duration.between(receivedAt, Instant.now(clock)).abs();
        if (diff.compareTo(TIMESTAMP_TOLERANCE) > 0) {
            throw new PaymentException("포트원 웹훅 timestamp 허용 범위를 초과했습니다.");
        }
    }

    private byte[] hmac(String signedContent) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(decodeSecret(), HMAC_SHA256));
            return mac.doFinal(signedContent.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new PaymentException("포트원 웹훅 시그니처 계산에 실패했습니다.", e);
        }
    }

    private byte[] decodeSecret() {
        String secret = properties.getWebhookSecret();
        String encoded = secret.startsWith(SECRET_PREFIX)
                ? secret.substring(SECRET_PREFIX.length())
                : secret;
        return Base64.getDecoder().decode(encoded);
    }
}
