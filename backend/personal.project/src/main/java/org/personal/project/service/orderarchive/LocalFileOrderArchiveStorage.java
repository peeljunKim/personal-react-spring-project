package org.personal.project.service.orderarchive;

import lombok.extern.slf4j.Slf4j;
import org.personal.project.properties.OrderArchiveProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Component
public class LocalFileOrderArchiveStorage implements OrderArchiveStorage {

    private final Path storagePath;

    public LocalFileOrderArchiveStorage(OrderArchiveProperties properties) {
        this.storagePath = Path.of(properties.getStoragePath());
    }

    @Override
    public synchronized ArchiveStorageResult store(String header, List<OrderArchiveCsvRow> rows) {
        if (rows.isEmpty()) {
            return new ArchiveStorageResult(0, Set.of(), Set.of(), Set.of());
        }

        Map<String, OrderArchiveCsvRow> distinctRows = new LinkedHashMap<>();
        for (OrderArchiveCsvRow row : rows) {
            distinctRows.putIfAbsent(row.archiveKey(), row);
        }

        Set<String> writtenKeys = new HashSet<>();
        Set<String> existingKeys = new HashSet<>();
        Set<Path> touchedFiles = new HashSet<>();

        distinctRows.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        OrderArchiveCsvRow::archiveDate,
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ))
                .forEach((archiveDate, archiveRows) -> {
                    Path file = storagePath.resolve(archiveDate + "_orders.csv");
                    touchedFiles.add(file);
                    Set<String> candidateKeys = archiveRows.stream()
                            .map(OrderArchiveCsvRow::archiveKey)
                            .collect(java.util.stream.Collectors.toCollection(HashSet::new));
                    Set<String> alreadyArchivedKeys = findExistingKeys(file, candidateKeys);
                    existingKeys.addAll(alreadyArchivedKeys);

                    List<OrderArchiveCsvRow> missingRows = archiveRows.stream()
                            .filter(row -> !alreadyArchivedKeys.contains(row.archiveKey()))
                            .toList();
                    appendMissingRows(file, header, missingRows);
                    missingRows.forEach(row -> writtenKeys.add(row.archiveKey()));
                });

        log.info("주문 아카이브 CSV 저장 완료. requestedRows={}, writtenRows={}, skippedRows={}, files={}",
                rows.size(), writtenKeys.size(), existingKeys.size(), touchedFiles);
        return new ArchiveStorageResult(rows.size(), writtenKeys, existingKeys, touchedFiles);
    }

    private Set<String> findExistingKeys(Path file, Set<String> candidateKeys) {
        if (!Files.exists(file) || candidateKeys.isEmpty()) {
            return Set.of();
        }

        Set<String> existingKeys = new HashSet<>();
        try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
            lines.skip(1)
                    .map(this::firstCsvColumn)
                    .filter(candidateKeys::contains)
                    .forEach(existingKeys::add);
        } catch (IOException e) {
            throw new IllegalStateException("주문 아카이브 CSV 기존 키 조회에 실패했습니다. file=" + file, e);
        }
        return existingKeys;
    }

    private void appendMissingRows(Path file, String header, List<OrderArchiveCsvRow> missingRows) {
        if (missingRows.isEmpty()) {
            return;
        }

        try {
            Files.createDirectories(file.getParent());
            boolean writeHeader = !Files.exists(file) || Files.size(file) == 0;
            List<String> lines = new ArrayList<>();
            if (writeHeader) {
                lines.add(header);
            } else if (!endsWithNewLine(file)) {
                lines.add("");
            }
            missingRows.stream()
                    .map(OrderArchiveCsvRow::content)
                    .forEach(lines::add);

            String content = String.join(System.lineSeparator(), lines) + System.lineSeparator();
            Files.writeString(
                    file,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new IllegalStateException("주문 아카이브 CSV 저장에 실패했습니다. file=" + file, e);
        }
    }

    private boolean endsWithNewLine(Path file) throws IOException {
        if (Files.size(file) == 0) {
            return true;
        }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.toFile(), "r")) {
            randomAccessFile.seek(randomAccessFile.length() - 1);
            int lastByte = randomAccessFile.read();
            return lastByte == '\n' || lastByte == '\r';
        }
    }

    private String firstCsvColumn(String line) {
        int commaIndex = line.indexOf(',');
        if (commaIndex < 0) {
            return line;
        }
        return line.substring(0, commaIndex);
    }
}
