package org.personal.project.service.orderarchive;

import org.personal.project.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderArchiveCandidate(
        Long orderId,
        String paymentId,
        OrderStatus status,
        String payMethod,
        LocalDateTime createdAt
) {
}
