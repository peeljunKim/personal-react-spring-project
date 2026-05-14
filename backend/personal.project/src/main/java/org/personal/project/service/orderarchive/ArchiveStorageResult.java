package org.personal.project.service.orderarchive;

import java.nio.file.Path;
import java.util.Set;

public record ArchiveStorageResult(
        int requestedRows,
        Set<String> writtenKeys,
        Set<String> existingKeys,
        Set<Path> touchedFiles
) {

    public Set<String> archivedKeys() {
        Set<String> archivedKeys = new java.util.HashSet<>(writtenKeys);
        archivedKeys.addAll(existingKeys);
        return archivedKeys;
    }
}
