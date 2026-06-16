package org.personal.project.dto.coupon.requst;

import jakarta.validation.constraints.NotBlank;

/**
 * 이벤트 쿠폰 발급 요청
 */
public record EventCouponIssueRequest(
        @NotBlank
        String requestKey) {
}
