package org.personal.project.service.coupon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.exception.CouponException;
import org.personal.project.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 결제 진행 중 쿠폰 임시 선점 처리
 *
 * MemberCoupon 상태는 변경하지 않고 Redis TTL 예약으로 같은 쿠폰의 중복 결제 준비를 막습니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponReservationService {

    private static final String KEY_PREFIX = "coupon:reservation:";

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @Value("${coupon.payment.reservation-ttl-seconds:900}")
    private long reservationTtlSeconds;

    @Value("${coupon.payment.reservation-ttl-jitter-seconds:60}")
    private long reservationTtlJitterSeconds;

    /**
     * 쿠폰 임시 예약 생성
     */
    public String reserve(
            Long memberCouponId,
            String memberId,
            Long orderId,
            String paymentId,
            String payMethod
    ) {
        Duration ttl = calculateTtl();
        LocalDateTime createdAt = LocalDateTime.now();
        String reservationToken = java.util.UUID.randomUUID().toString();
        ReservationPayload payload = new ReservationPayload(
                memberCouponId,
                memberId,
                orderId,
                paymentId,
                payMethod,
                reservationToken,
                createdAt,
                createdAt.plus(ttl)
        );
        String key = key(memberCouponId);
        String value = toJson(payload);

        boolean created = redisService.setIfAbsent(key, value, ttl);
        if (!created) {
            throw new CouponException("이미 결제 진행 중인 쿠폰입니다. memberCouponId=" + memberCouponId);
        }

        log.info("쿠폰 Redis 예약 생성 memberCouponId={}, orderId={}, paymentId={}, expiresAt={}",
                memberCouponId, orderId, paymentId, payload.expiresAt());
        return reservationToken;
    }

    /**
     * 쿠폰 예약 여부 확인
     */
    public boolean isReserved(Long memberCouponId) {
        return redisService.exists(key(memberCouponId));
    }

    /**
     * 결제 확정 전 예약 정보 검증
     */
    public void validateReservation(Long memberCouponId, Long orderId, String paymentId) {
        ReservationPayload payload = findPayload(memberCouponId)
                .orElseThrow(() -> new CouponException("쿠폰 결제 예약 정보가 만료되었거나 존재하지 않습니다. memberCouponId="
                        + memberCouponId));

        if (!payload.orderId().equals(orderId) || !payload.paymentId().equals(paymentId)) {
            throw new CouponException("쿠폰 결제 예약 정보가 현재 주문과 일치하지 않습니다. memberCouponId="
                    + memberCouponId + ", orderId=" + orderId + ", paymentId=" + paymentId);
        }
    }

    /**
     * paymentId가 일치하는 예약 해제
     */
    public boolean releaseByPayment(Long memberCouponId, String paymentId) {
        Optional<String> value = redisService.get(key(memberCouponId));
        if (value.isEmpty()) {
            return false;
        }

        ReservationPayload payload = fromJson(value.get());
        if (!payload.paymentId().equals(paymentId)) {
            return false;
        }

        return redisService.deleteIfValueEquals(key(memberCouponId), value.get());
    }

    /**
     * reservationToken이 일치하는 예약 해제
     */
    public boolean release(Long memberCouponId, String reservationToken) {
        Optional<String> value = redisService.get(key(memberCouponId));
        if (value.isEmpty()) {
            return false;
        }

        ReservationPayload payload = fromJson(value.get());
        if (!payload.reservationToken().equals(reservationToken)) {
            return false;
        }

        return redisService.deleteIfValueEquals(key(memberCouponId), value.get());
    }

    private Optional<ReservationPayload> findPayload(Long memberCouponId) {
        return redisService.get(key(memberCouponId))
                .map(this::fromJson);
    }

    private String key(Long memberCouponId) {
        return KEY_PREFIX + memberCouponId;
    }

    private Duration calculateTtl() {
        long jitter = reservationTtlJitterSeconds <= 0
                ? 0
                : ThreadLocalRandom.current().nextLong(reservationTtlJitterSeconds + 1);
        return Duration.ofSeconds(reservationTtlSeconds + jitter);
    }

    private String toJson(ReservationPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new CouponException("쿠폰 예약 정보를 JSON으로 변환하지 못했습니다.", e);
        }
    }

    private ReservationPayload fromJson(String value) {
        try {
            return objectMapper.readValue(value, ReservationPayload.class);
        } catch (JsonProcessingException e) {
            throw new CouponException("쿠폰 예약 정보를 읽을 수 없습니다.", e);
        }
    }

    private record ReservationPayload(
            Long memberCouponId,
            String memberId,
            Long orderId,
            String paymentId,
            String payMethod,
            String reservationToken,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
    }
}
