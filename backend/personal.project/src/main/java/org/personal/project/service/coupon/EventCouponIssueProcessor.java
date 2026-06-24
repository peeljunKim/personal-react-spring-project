package org.personal.project.service.coupon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.dto.coupon.CouponIssueMessage;
import org.personal.project.entity.coupon.CouponIssueRequestStatus;
import org.personal.project.exception.CouponException;
import org.personal.project.repository.coupon.CouponIssueRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 이벤트 쿠폰 발급 메시지 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventCouponIssueProcessor {

    private final CouponIssueService couponIssueService;
    private final CouponIssueRequestRepository couponIssueRequestRepository;

    /**
     * 이벤트 쿠폰 발급 메시지 처리
     */
    @Transactional
    public void process(CouponIssueMessage message) {
        log.info("이벤트 쿠폰 발급 처리 시작 requestKey={}, policyId={}, memberId={}",
                message.requestKey(), message.policyId(), message.memberId());

        LocalDateTime now = LocalDateTime.now();
        int started = couponIssueRequestRepository.markProcessingIfPending(
                message.requestKey(),
                CouponIssueRequestStatus.PENDING,
                CouponIssueRequestStatus.PROCESSING,
                now
        );

        if (started != 1) {
            log.warn("이벤트 쿠폰 발급 처리 스킵 requestKey={}, policyId={}, memberId={}",
                    message.requestKey(), message.policyId(), message.memberId());
            return;
        }

        try {
            couponIssueService.issueFCFSCoupon(message.policyId(), message.memberId(), message.requestKey());
            couponIssueRequestRepository.markSucceededIfProcessing(
                    message.requestKey(),
                    CouponIssueRequestStatus.PROCESSING,
                    CouponIssueRequestStatus.SUCCEEDED,
                    LocalDateTime.now()
            );

            log.info("이벤트 쿠폰 발급 처리 성공 requestKey={}, policyId={}, memberId={}",
                    message.requestKey(), message.policyId(), message.memberId());
        } catch (CouponException e) {
            couponIssueRequestRepository.markFailedIfNotCompleted(
                    message.requestKey(),
                    List.of(CouponIssueRequestStatus.PROCESSING),
                    CouponIssueRequestStatus.FAILED,
                    e.getMessage(),
                    LocalDateTime.now()
            );

            log.warn("이벤트 쿠폰 발급 처리 실패 requestKey={}, policyId={}, memberId={}, reason={}",
                    message.requestKey(), message.policyId(), message.memberId(), e.getMessage());
        }
    }
}
