package org.personal.project.service.orderarchive;

import java.time.LocalDate;

public record OrderArchiveCsvRow(
        Long orderId,
        String archiveKey,
        LocalDate archiveDate,
        String content
) {
}
