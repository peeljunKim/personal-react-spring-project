package org.personal.project.service.orderarchive;

public record OrderArchiveRunResult(
        int scannedOrders,
        int archivedRows,
        int skippedRows,
        int purgedOrders
) {
}
