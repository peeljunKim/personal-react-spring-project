package org.personal.project.service.orderarchive;

import java.util.List;

public interface OrderArchiveStorage {

    ArchiveStorageResult store(String header, List<OrderArchiveCsvRow> rows);
}
