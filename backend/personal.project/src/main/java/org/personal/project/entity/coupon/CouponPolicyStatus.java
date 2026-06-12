package org.personal.project.entity.coupon;

/**
 * 쿠폰 정책 상태
 */
public enum CouponPolicyStatus {
    DRAFT,        // 정책 작성 중
    ACTIVE,       // 발급/사용 가능
    PAUSED,       // 정책 일시 중지
    ISSUE_CLOSED, // 신규 발급 종료
    CANCELED      // 정책 회수
}
