package org.personal.project.service.orderarchive;

import org.personal.project.entity.OrderStatus;
import org.personal.project.entity.PaymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrderArchiveRecord(
        Long orderId,
        String paymentId,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        String paymentProviderStatus,
        String paymentFailureReason,
        String payMethod,
        Integer orderAmount,
        LocalDateTime orderCreatedAt,
        LocalDateTime orderUpdatedAt,
        LocalDateTime paymentVerifiedAt,
        LocalDateTime paidAt,
        LocalDateTime cancelledAt,
        String memberId,
        Long orderItemId,
        Long productId,
        String productName,
        Integer productPrice,
        Integer quantity,
        Integer lineAmount
) {

    private static final String ORDER_ONLY_ITEM_KEY = "ORDER";

    public String archiveKey() {
        return orderId + ":" + (orderItemId == null ? ORDER_ONLY_ITEM_KEY : orderItemId);
    }

    public LocalDate archiveDate() {
        return orderCreatedAt.toLocalDate();
    }
}
