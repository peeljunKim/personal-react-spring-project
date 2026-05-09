package org.personal.project.dto.payment;

import lombok.Builder;

@Builder
public record PaymentSyncResponse(
        String paymentId,
        String orderStatus,
        String paymentStatus,
        String message) {
}
