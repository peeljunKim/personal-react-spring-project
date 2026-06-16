package org.personal.project.entity.coupon;

/**
 * 쿠폰 정책 상태
 */
public enum CouponPolicyStatus {
    DRAFT,        // 쿠폰 정책 생성 후 대기
    ACTIVE,       // 발급/사용 가능
    PAUSED,       // 정책 일시 중지
    ISSUE_CLOSED, // 이벤트 발급 종료 (사용 가능 단. 기간 등 여러 정책에 따라 사용 종료 될 수 있음)
    CANCELED      // 정책 회수
}
