package org.personal.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.annotation.DistributedLock;
import org.springframework.stereotype.Service;

/**
 * 비즈니스 로직에 분산 락을 적용한 서비스 클래스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class LockService {

    /**
     * 분산 락을 이용한 처리 메서드
     * <p> -동시에 여러 스레드가 접근하면 하나만 실행되고 나머지는 실패 처리됨
     */
    @DistributedLock(key = "#userId", waitTime = 100, leaseTime = 3000)
    public void executeWithLock(String userId) {
        try {
            Thread.sleep(1000); // 락 점유 시간 확보
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 분산 락을 이용하지 않은 메서드
     */
    public void executeWithoutLock(String userId) {
        try {
            Thread.sleep(1000); // 락 점유 시간 확보
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}