package org.personal.project.service.orderarchive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.personal.project.properties.OrderArchiveProperties;
import org.personal.project.service.payment.PaymentLockExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderArchiveService {

    private final OrderArchiveExtractor extractor;
    private final OrderArchiveConverter converter;
    private final OrderArchiveStorage storage;
    private final OrderArchivePurger purger;
    private final OrderArchivePolicy policy;
    private final PaymentLockExecutor lockExecutor;
    private final OrderArchiveProperties properties;

    public OrderArchiveRunResult archiveEligibleOrders() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(properties.getZoneId()));
        Long lastOrderId = null;
        AtomicInteger scannedOrders = new AtomicInteger();
        AtomicInteger archivedRows = new AtomicInteger();
        AtomicInteger skippedRows = new AtomicInteger();
        AtomicInteger purgedOrders = new AtomicInteger();

        for (int page = 0; page < properties.getMaxPagesPerRun(); page++) {
            OrderArchiveBatch batch = extractor.extract(policy.criteria(now, lastOrderId));
            if (batch.isEmpty()) {
                break;
            }

            lastOrderId = batch.lastOrderId();
            scannedOrders.addAndGet(batch.candidates().size());
            PageArchiveResult pageResult = archivePage(batch, now);
            archivedRows.addAndGet(pageResult.archivedRows());
            skippedRows.addAndGet(pageResult.skippedRows());
            purgedOrders.addAndGet(pageResult.purgedOrders());

            if (batch.candidates().size() < properties.getPageSize()) {
                break;
            }
        }

        OrderArchiveRunResult result = new OrderArchiveRunResult(
                scannedOrders.get(),
                archivedRows.get(),
                skippedRows.get(),
                purgedOrders.get()
        );
        log.info("주문 이탈 데이터 아카이브 배치 완료. result={}", result);
        return result;
    }

    private PageArchiveResult archivePage(OrderArchiveBatch batch, LocalDateTime now) {
        List<String> lockKeys = batch.candidates().stream()
                .map(OrderArchiveCandidate::paymentId)
                .filter(Objects::nonNull)
                .map(paymentId -> "payment:sync:" + paymentId)
                .toList();

        return lockExecutor.execute(
                lockKeys,
                properties.getLockWait().toMillis(),
                properties.getLockLease().toMillis(),
                () -> archiveLockedPage(batch, now)
        );
    }

    private PageArchiveResult archiveLockedPage(OrderArchiveBatch batch, LocalDateTime now) {
        Set<Long> currentEligibleOrderIds = extractor.findCurrentCandidates(batch.orderIds()).stream()
                .filter(candidate -> policy.isEligible(candidate, now))
                .map(OrderArchiveCandidate::orderId)
                .collect(Collectors.toCollection(HashSet::new));
        if (currentEligibleOrderIds.isEmpty()) {
            return new PageArchiveResult(0, batch.records().size(), 0);
        }

        List<OrderArchiveRecord> eligibleRecords = batch.records().stream()
                .filter(record -> currentEligibleOrderIds.contains(record.orderId()))
                .toList();
        List<OrderArchiveCsvRow> rows = converter.convert(eligibleRecords, now);
        ArchiveStorageResult storageResult = storage.store(converter.header(), rows);
        Set<String> archivedKeys = storageResult.archivedKeys();
        Set<Long> archivedOrderIds = resolveCompletelyArchivedOrderIds(rows, archivedKeys);

        OrderArchivePurgeResult purgeResult = purger.purge(archivedOrderIds, policy, now);
        return new PageArchiveResult(archivedKeys.size(), storageResult.existingKeys().size(), purgeResult.purgedCount());
    }

    private Set<Long> resolveCompletelyArchivedOrderIds(List<OrderArchiveCsvRow> rows, Set<String> archivedKeys) {
        Map<Long, Set<String>> expectedKeysByOrderId = new HashMap<>();
        for (OrderArchiveCsvRow row : rows) {
            expectedKeysByOrderId.computeIfAbsent(row.orderId(), ignored -> new HashSet<>()).add(row.archiveKey());
        }

        Set<Long> archivedOrderIds = new HashSet<>();
        expectedKeysByOrderId.forEach((orderId, expectedKeys) -> {
            if (archivedKeys.containsAll(expectedKeys)) {
                archivedOrderIds.add(orderId);
            }
        });
        return archivedOrderIds;
    }

    private record PageArchiveResult(int archivedRows, int skippedRows, int purgedOrders) {
    }
}
