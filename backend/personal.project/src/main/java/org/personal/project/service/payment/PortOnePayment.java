package org.personal.project.service.payment;

public record PortOnePayment(
        String id,
        String status,
        Integer totalAmount,
        String transactionId
) {
    public boolean isPaid() {
        return "PAID".equals(status);
    }

    public boolean isFailedOrCancelled() {
        return "FAILED".equals(status)
                || "CANCELLED".equals(status)
                || "PARTIAL_CANCELLED".equals(status);
    }
}
