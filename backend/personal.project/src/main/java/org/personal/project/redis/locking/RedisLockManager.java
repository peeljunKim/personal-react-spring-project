package org.personal.project.redis.locking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_PREFIX = "lock:";
    private static final String CHANNEL_PREFIX = "lock_channel:";

    // 락의 소유권(UUID) 확인 후 안전하게 삭제
    private static final String SAVE_DELETE_LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    redis.call('del', KEYS[1]) " +
                    "    redis.call('publish', KEYS[2], 'released') " +
                    "    return 1 " +
                    "else " +
                    "    return 0 " +
                    "end";

    /**
     * 분산 락 획득 시도 (SET NX PX)
     */
    public String acquireLock(String key, long timeoutMs) {
        String lockKey = LOCK_PREFIX + key;
        String uuid = UUID.randomUUID().toString();

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, uuid, timeoutMs, TimeUnit.MILLISECONDS);

        return Boolean.TRUE.equals(success) ? uuid : null;
    }

    /**
     * 락 획득 실패 시 Pub/Sub 기반 대기 전략
     */
    public void waitForLockWithPubSub(String key, long waitTimeoutMs) {
        String channel = CHANNEL_PREFIX + key;
        CountDownLatch latch = new CountDownLatch(1);

        // Redis Pub/Sub 구독을 통한 대기
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.subscribe((message, pattern) -> {
                latch.countDown(); // 신호 받으면 대기 해제
            }, channel.getBytes());
            return null;
        });

        try {
            latch.await(waitTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Lua Script를 이용한 안전한 락 해제
     */
    public void releaseLock(String key, String uuid) {
        String lockKey = LOCK_PREFIX + key;
        String channel = CHANNEL_PREFIX + key;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(SAVE_DELETE_LUA_SCRIPT, Long.class);
        redisTemplate.execute(script, Collections.unmodifiableList(java.util.List.of(lockKey, channel)), uuid);
    }
}
