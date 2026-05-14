package org.personal.project.service.orderarchive;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderArchiveConverter {

    String header();

    List<OrderArchiveCsvRow> convert(List<OrderArchiveRecord> records, LocalDateTime archivedAt);
}
