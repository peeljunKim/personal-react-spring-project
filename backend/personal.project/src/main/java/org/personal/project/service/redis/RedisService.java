package org.personal.project.service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Redis 문자열 값 공통 처리
 * <p>
 * 쿠폰 예약, 인증 코드처럼 단순 문자열 값을 저장하는 흐름에서 사용
 */
@Service
@RequiredArgsConstructor
public class RedisService {

    private static final DefaultRedisScript<Long> DELETE_IF_VALUE_EQUALS_SCRIPT = new DefaultRedisScript<>(
            """
                    local value = redis.call('GET', KEYS[1])
                    if value == false then
                        return 0
                    end
                    if value == ARGV[1] then
                        return redis.call('DEL', KEYS[1])
                    end
                    return 0
                    """,
            Long.class
    );

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * TTL이 있는 값 저장
     */
    public void set(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 키가 없을 때만 값 저장
     */
    public boolean setIfAbsent(String key, String value, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, ttl));
    }

    /**
     * 문자열 값 조회
     */
    public Optional<String> get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value.toString());
    }

    /**
     * 키 존재 여부
     */
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 값 삭제
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * expectedValue 값이 같을 때만 삭제
     */
    public boolean deleteIfValueEquals(String key, String expectedValue) {
        Long result = redisTemplate.execute(
                DELETE_IF_VALUE_EQUALS_SCRIPT,
                List.of(key),
                expectedValue
        );
        return result != null && result == 1L;
    }
}
