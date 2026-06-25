package org.personal.project.service.coupon;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 쿠폰 만료 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponExpirationScheduler {

    private final CouponExpirationService couponExpirationService;

    @Value("${coupon.expiration.cron:0 0 0 * * *}")
    private String cron;

    @Value("${coupon.expiration.zone-id:Asia/Seoul}")
    private String zoneId;

    /**
     * 쿠폰 만료 배치 설정 로그 (cron, zoneId)
     */
    @PostConstruct
    public void logScheduleConfig() {
        log.info("쿠폰 만료 배치 설정 cron={}, zoneId={}", cron, zoneId);
    }

    /**
     * 만료 쿠폰 정리
     * <p>기본 실행 시간은 매일 0시 0분 0초</p>
     */
    @Scheduled(cron = "${coupon.expiration.cron:0 0 0 * * *}", zone = "${coupon.expiration.zone-id:Asia/Seoul}")
    public void expireIssuedCoupons() {
        couponExpirationService.expireIssuedCoupons(LocalDateTime.now(ZoneId.of(zoneId)));
    }
}
