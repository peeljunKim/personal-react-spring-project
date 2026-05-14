package org.personal.project.service.orderarchive;

import java.util.List;

public record OrderArchivePurgeResult(
        int requestedOrders,
        List<Long> purgedOrderIds,
        List<Long> skippedOrderIds
) {

    public int purgedCount() {
        return purgedOrderIds.size();
    }
}
