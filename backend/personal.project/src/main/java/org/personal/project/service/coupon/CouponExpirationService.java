package org.personal.project.service.coupon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.entity.coupon.CouponPolicyStatus;
import org.personal.project.entity.coupon.MemberCouponStatus;
import org.personal.project.repository.coupon.MemberCouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 쿠폰 기간 만료 처리
 * <p>
 * 사용 종료 시간이 지난 사용자 쿠폰을 EXPIRED 상태로 전환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponExpirationService {

    private static final List<CouponPolicyStatus> EXPIRABLE_POLICY_STATUSES = List.of(
            CouponPolicyStatus.ACTIVE,
            CouponPolicyStatus.PAUSED,
            CouponPolicyStatus.ISSUE_CLOSED
    );

    private final MemberCouponRepository memberCouponRepository;

    /**
     * 만료 대상 쿠폰 상태 변경
     */
    @Transactional
    public int expireIssuedCoupons(LocalDateTime now) {
        long startedAt = System.currentTimeMillis();

        int expiredCount = memberCouponRepository.expireIssuedCoupons(
                MemberCouponStatus.ISSUED,
                MemberCouponStatus.EXPIRED,
                EXPIRABLE_POLICY_STATUSES,
                now
        );

        log.info("쿠폰 만료 배치 완료 now={}, expiredCount={}, elapsedMs={}",
                now, expiredCount, System.currentTimeMillis() - startedAt);

        return expiredCount;
    }
}
