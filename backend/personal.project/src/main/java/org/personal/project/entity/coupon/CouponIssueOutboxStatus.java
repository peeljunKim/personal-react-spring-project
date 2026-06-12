package org.personal.project.entity.coupon;

/**
 * 쿠폰 발급 Outbox 상태
 */
public enum CouponIssueOutboxStatus {
    PENDING,         // 메시지 발행 대기
    PUBLISHING,      // 메시지 발행 중
    PUBLISHED,       // 메시지 발행 성공
    PUBLISH_FAILED,  // 메시지 발행 실패
    CONFIRM_UNKNOWN, // 발행 결과 불명확
    FAILED           // 최종 발행 실패
}
