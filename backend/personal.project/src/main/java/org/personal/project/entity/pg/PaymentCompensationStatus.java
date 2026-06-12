package org.personal.project.entity.pg;

/**
 * 결제 보상 상태
 */
public enum PaymentCompensationStatus {
    NONE,                // 보상 없음
    CANCEL_REQUIRED,     // PG 취소 필요
    CANCEL_REQUESTED,    // PG 취소 요청
    COMPENSATION_FAILED, // 보상 실패
    COMPENSATED          // 보상 완료
}
