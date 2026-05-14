package org.personal.project.service.orderarchive;

import org.personal.project.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record OrderArchiveCriteria(
        LocalDateTime immediateCutoff,
        LocalDateTime delayedCutoff,
        LocalDateTime unknownCutoff,
        Set<String> immediatePayMethods,
        Set<String> delayedPayMethods,
        Set<String> knownPayMethods,
        String defaultPayMethod,
        List<OrderStatus> archivableStatuses,
        Long lastOrderId,
        int pageSize
) {
}
