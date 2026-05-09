package org.personal.project.dto.payment;

import lombok.Builder;

@Builder
public record PaymentPrepareResponse(
        Long orderId,
        String paymentId,
        String orderName,
        Integer totalAmount,
        String currency,
        String payMethod,
        String storeId,
        String channelKey,
        String noticeUrl
) {
}
