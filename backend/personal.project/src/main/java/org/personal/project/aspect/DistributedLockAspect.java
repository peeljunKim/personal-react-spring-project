package org.personal.project.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.personal.project.annotation.DistributedLock;
import org.personal.project.util.CustomSpringELParser;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private static final String LOCK_PREFIX = "lock:";

    /**
     * 메서드 실행 전 락을 획득하고, 실행 후 해제
     *
     * @param joinPoint 실행 대상 메서드
     * @return 메서드 실행 결과
     * @throws Throwable 예외 발생 시
     */

    @Around("@annotation(org.personal.project.annotation.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        DistributedLock distributedLock = signature.getMethod().getAnnotation(DistributedLock.class);

        // SpEL을 사용해 락 키를 파싱 (예: "product:123")
        String lockKey = LOCK_PREFIX + CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(),
                joinPoint.getArgs(),
                distributedLock.key()
        );

        RLock rLock = redissonClient.getLock(lockKey);

        try {
            // 2. 락 획득 시도 (Redisson의 Pub/Sub 기반 대기)
            boolean available = rLock.tryLock(
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit()
            );

            if (!available) {
                log.warn("[RedissonLock] 락 획득 실패: {}", lockKey);
                throw new IllegalStateException("락 획득 실패 - lockKey: " + lockKey);
            }

            // 3. 비즈니스 로직 실행
            log.debug("[RedissonLock] 락 획득 성공 - lockKey: {}", lockKey);
            return joinPoint.proceed();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트 발생", e);
        } finally {
            // 4. 안전한 해제 (내가 건 락일 때만 해제)
            if (rLock.isHeldByCurrentThread()) {
                try {
                    rLock.unlock();
                    log.debug("[RedissonLock] 락 해제 완료 - lockKey: {}", lockKey);
                } catch (IllegalMonitorStateException e) {
                    log.warn("[RedissonLock] 이미 해제된 락 또는 스레드 불일치 - lockKey: {}", lockKey, e);
                }
            }
        }
    }
}