package org.personal.project.service.orderarchive;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CsvOrderArchiveConverter implements OrderArchiveConverter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String HEADER = String.join(",",
            "archive_key",
            "order_id",
            "payment_id",
            "order_status",
            "pay_method",
            "order_amount",
            "order_created_at",
            "paid_at",
            "cancelled_at",
            "member_id",
            "order_item_id",
            "product_id",
            "product_name",
            "product_price",
            "quantity",
            "line_amount",
            "archived_at"
    );

    @Override
    public String header() {
        return HEADER;
    }

    @Override
    public List<OrderArchiveCsvRow> convert(List<OrderArchiveRecord> records, LocalDateTime archivedAt) {
        return records.stream()
                .map(record -> new OrderArchiveCsvRow(
                        record.orderId(),
                        record.archiveKey(),
                        record.archiveDate(),
                        toCsvLine(record, archivedAt)
                ))
                .toList();
    }

    private String toCsvLine(OrderArchiveRecord record, LocalDateTime archivedAt) {
        return String.join(",",
                csv(record.archiveKey()),
                csv(record.orderId()),
                csv(record.paymentId()),
                csv(record.orderStatus()),
                csv(record.payMethod()),
                csv(record.orderAmount()),
                csv(record.orderCreatedAt()),
                csv(record.paidAt()),
                csv(record.cancelledAt()),
                csv(record.memberId()),
                csv(record.orderItemId()),
                csv(record.productId()),
                csv(record.productName()),
                csv(record.productPrice()),
                csv(record.quantity()),
                csv(record.lineAmount()),
                csv(archivedAt)
        );
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = value instanceof LocalDateTime dateTime
                ? dateTime.format(DATE_TIME_FORMATTER)
                : String.valueOf(value);
        text = neutralizeSpreadsheetFormula(text);
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private String neutralizeSpreadsheetFormula(String text) {
        if (text.isEmpty()) {
            return text;
        }
        char first = text.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
            return "'" + text;
        }
        return text;
    }
}
