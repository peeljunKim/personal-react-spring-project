package org.personal.project.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 분산 락 추가
 * <p>paymentId/상품별 재고 키로 락 세분화</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentLockExecutor {

    private static final String LOCK_PREFIX = "lock:";

    private final RedissonClient redissonClient;

    public <T> T execute(List<String> keys, long waitMillis, long leaseMillis, Supplier<T> supplier) {
        List<String> lockKeys = keys.stream()
                .distinct()
                .sorted()
                .map(key -> LOCK_PREFIX + key)
                .toList();

        if (lockKeys.isEmpty()) {
            return supplier.get();
        }

        RLock lock = createLock(lockKeys);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitMillis, leaseMillis, TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new PaymentException("결제 처리를 위한 락 획득에 실패했습니다. lockKeys=" + lockKeys);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentException("결제 락 획득 중 인터럽트가 발생했습니다.", e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("결제 락 해제 완료. lockKeys={}", lockKeys);
            }
        }
    }

    private RLock createLock(List<String> lockKeys) {
        if (lockKeys.size() == 1) {
            return redissonClient.getLock(lockKeys.get(0));
        }

        RLock[] locks = lockKeys.stream()
                .map(redissonClient::getLock)
                .toArray(RLock[]::new);
        return redissonClient.getMultiLock(locks);
    }
}
