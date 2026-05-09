package org.personal.project.service.payment;

import lombok.RequiredArgsConstructor;
import org.personal.project.repository.PortOneWebhookLogRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class WebhookIdempotencyService {

    private static final String KEY_PREFIX = "idempotency:portone:webhook:";
    private static final String PROCESSING = "PROCESSING";
    private static final String PROCESSED = "PROCESSED";
    private static final Duration PROCESSING_TTL = Duration.ofMinutes(10);
    private static final Duration PROCESSED_TTL = Duration.ofDays(7);

    private final RedisTemplate<String, Object> redisTemplate;
    private final PortOneWebhookLogRepository webhookLogRepository;

    public boolean tryStart(String webhookId) {
        if (webhookLogRepository.existsByWebhookId(webhookId)) {
            return false;
        }

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key(webhookId), PROCESSING, PROCESSING_TTL);
        return Boolean.TRUE.equals(acquired);
    }

    public void markProcessed(String webhookId) {
        redisTemplate.opsForValue().set(key(webhookId), PROCESSED, PROCESSED_TTL);
    }

    public void release(String webhookId) {
        redisTemplate.delete(key(webhookId));
    }

    private String key(String webhookId) {
        return KEY_PREFIX + webhookId;
    }
}
