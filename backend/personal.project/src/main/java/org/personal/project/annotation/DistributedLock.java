package org.personal.project.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;


/**
 * 분산 락 적용용 어노테이션
 * <p> - 특정 메서드 실행 시 Redisson 분산 락을 획득/반환하도록 처리
 */
@Target(ElementType.METHOD)         // 메서드에만 적용 가능
@Retention(RetentionPolicy.RUNTIME) // 런타임 시점 접근 가능
@Documented
public @interface DistributedLock {

    String key();                   // 락의 이름 (SpEL 사용 가능)

    long waitTime() default 5000L;  // 락 획득 대기 시간 (ms)

    long leaseTime() default 3000L; // 락 점유 시간 (ms)

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}