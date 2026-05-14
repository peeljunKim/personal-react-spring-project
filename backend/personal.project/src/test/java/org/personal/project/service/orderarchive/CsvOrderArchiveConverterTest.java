package org.personal.project.service.orderarchive;

import org.junit.jupiter.api.Test;
import org.personal.project.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvOrderArchiveConverterTest {

    @Test
    void convertsOrderRecordToEscapedCsvRow() {
        CsvOrderArchiveConverter converter = new CsvOrderArchiveConverter();
        OrderArchiveRecord record = new OrderArchiveRecord(
                10L,
                "payment-10",
                OrderStatus.READY,
                "CARD",
                12000,
                LocalDateTime.parse("2026-05-14T10:00:00"),
                null,
                null,
                "buyer@example.com",
                20L,
                30L,
                "Sneaker, Limited",
                6000,
                2,
                12000
        );

        List<OrderArchiveCsvRow> rows = converter.convert(List.of(record), LocalDateTime.parse("2026-05-14T12:00:00"));

        assertEquals(1, rows.size());
        assertEquals("10:20", rows.get(0).archiveKey());
        assertTrue(rows.get(0).content().contains("\"Sneaker, Limited\""));
        assertTrue(rows.get(0).content().endsWith("2026-05-14T12:00:00"));
    }

    @Test
    void neutralizesSpreadsheetFormulaInjection() {
        CsvOrderArchiveConverter converter = new CsvOrderArchiveConverter();
        OrderArchiveRecord record = new OrderArchiveRecord(
                10L,
                "=cmd",
                OrderStatus.READY,
                "CARD",
                12000,
                LocalDateTime.parse("2026-05-14T10:00:00"),
                null,
                null,
                "@buyer",
                20L,
                30L,
                "+SUM(1,1)",
                6000,
                2,
                12000
        );

        OrderArchiveCsvRow row = converter.convert(List.of(record), LocalDateTime.parse("2026-05-14T12:00:00")).get(0);

        assertTrue(row.content().contains("'=cmd"));
        assertTrue(row.content().contains("'@buyer"));
        assertTrue(row.content().contains("\"'+SUM(1,1)\""));
    }
}
