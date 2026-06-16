package org.personal.project.service.coupon;

import lombok.RequiredArgsConstructor;
import org.personal.project.entity.coupon.CouponIssueHistory;
import org.personal.project.repository.coupon.CouponIssueHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 쿠폰 발급 이력 기록
 */
@Service
@RequiredArgsConstructor
public class CouponIssueHistoryRecorder {

    private final CouponIssueHistoryRepository couponIssueHistoryRepository;

    /**
     * 발급 결과 이력 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            Long policyId,
            String policyName,
            Integer discountAmount,
            String memberId,
            String requestKey,
            String result,
            String reason
    ) {
        couponIssueHistoryRepository.save(CouponIssueHistory.builder()
                .policyId(policyId)
                .policyName(policyName)
                .discountAmount(discountAmount)
                .memberId(memberId)
                .requestKey(requestKey)
                .result(result)
                .reason(reason)
                .build());
    }
}
