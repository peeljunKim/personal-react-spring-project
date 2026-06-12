package org.personal.project.entity.coupon;

/**
 * 쿠폰 발급 요청 상태
 */
public enum CouponIssueRequestStatus {
    PENDING,    // 발급 요청 접수
    PROCESSING, // Consumer 처리 중
    SUCCEEDED,  // 쿠폰 발급 성공
    FAILED      // 쿠폰 발급 실패
}
