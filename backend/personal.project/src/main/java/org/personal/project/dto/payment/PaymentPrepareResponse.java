package org.personal.project.dto.payment;

import lombok.Builder;

/**
 * 결제 준비 응답 값
 */
@Builder
public record PaymentPrepareResponse(
        Long orderId,
        String paymentId,
        String orderName,
        Integer totalAmount,
        Integer originalAmount,
        Integer discountAmount,
        Integer payableAmount,
        String currency,
        String payMethod,
        String storeId,
        String channelKey,
        String noticeUrl
) {
}
