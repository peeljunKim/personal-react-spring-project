package org.personal.project.service.orderarchive;

import java.util.List;

public record OrderArchiveBatch(
        List<OrderArchiveCandidate> candidates,
        List<OrderArchiveRecord> records
) {

    public boolean isEmpty() {
        return candidates.isEmpty();
    }

    public Long lastOrderId() {
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(candidates.size() - 1).orderId();
    }

    public List<Long> orderIds() {
        return candidates.stream()
                .map(OrderArchiveCandidate::orderId)
                .toList();
    }
}
