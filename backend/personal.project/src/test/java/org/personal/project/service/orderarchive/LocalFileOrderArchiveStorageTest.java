package org.personal.project.service.orderarchive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.personal.project.properties.OrderArchiveProperties;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileOrderArchiveStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void storesRowsByArchiveDateAndSkipsDuplicateKeys() throws Exception {
        OrderArchiveProperties properties = new OrderArchiveProperties();
        properties.setStoragePath(tempDir.toString());
        LocalFileOrderArchiveStorage storage = new LocalFileOrderArchiveStorage(properties);
        List<OrderArchiveCsvRow> rows = List.of(
                new OrderArchiveCsvRow(1L, "1:10", LocalDate.parse("2026-05-14"), "1:10,1,payment-1"),
                new OrderArchiveCsvRow(1L, "1:11", LocalDate.parse("2026-05-14"), "1:11,1,payment-1")
        );

        ArchiveStorageResult first = storage.store("archive_key,order_id,payment_id", rows);
        ArchiveStorageResult second = storage.store("archive_key,order_id,payment_id", rows);

        Path archiveFile = tempDir.resolve("2026-05-14_orders.csv");
        List<String> lines = Files.readAllLines(archiveFile, StandardCharsets.UTF_8);
        assertEquals(2, first.writtenKeys().size());
        assertEquals(0, first.existingKeys().size());
        assertEquals(0, second.writtenKeys().size());
        assertEquals(2, second.existingKeys().size());
        assertEquals(3, lines.size());
        assertTrue(lines.contains("1:10,1,payment-1"));
        assertTrue(lines.contains("1:11,1,payment-1"));
    }
}
