package org.personal.project.service.orderarchive;

import org.personal.project.entity.Order;
import org.personal.project.properties.OrderArchiveProperties;
import org.personal.project.service.payment.PortOnePayMethodResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrderArchivePolicy {

    private static final String UNKNOWN_METHOD = "UNKNOWN";
    private static final String NO_MATCH_METHOD = "__NO_MATCH__";

    private final OrderArchiveProperties archiveProperties;
    private final PortOnePayMethodResolver payMethodResolver;

    public OrderArchivePolicy(OrderArchiveProperties archiveProperties, PortOnePayMethodResolver payMethodResolver) {
        this.archiveProperties = archiveProperties;
        this.payMethodResolver = payMethodResolver;
    }

    public OrderArchiveCriteria criteria(LocalDateTime now, Long lastOrderId) {
        Set<String> immediateMethods = normalizeAll(archiveProperties.getImmediatePayMethods());
        Set<String> delayedMethods = normalizeAll(archiveProperties.getDelayedPayMethods());
        if (immediateMethods.isEmpty()) {
            immediateMethods.add(NO_MATCH_METHOD + "_IMMEDIATE");
        }
        if (delayedMethods.isEmpty()) {
            delayedMethods.add(NO_MATCH_METHOD + "_DELAYED");
        }
        Set<String> knownMethods = new LinkedHashSet<>(immediateMethods);
        knownMethods.addAll(delayedMethods);
        if (knownMethods.isEmpty()) {
            knownMethods.add(UNKNOWN_METHOD);
        }

        return new OrderArchiveCriteria(
                now.minus(archiveProperties.getImmediateRetention()),
                now.minus(archiveProperties.getDelayedRetention()),
                now.minus(archiveProperties.getUnknownRetention()),
                immediateMethods,
                delayedMethods,
                knownMethods,
                defaultPayMethod(),
                archiveProperties.getArchivableStatuses(),
                lastOrderId,
                archiveProperties.getPageSize()
        );
    }

    public boolean isEligible(OrderArchiveCandidate candidate, LocalDateTime now) {
        if (!archiveProperties.getArchivableStatuses().contains(candidate.status())) {
            return false;
        }
        return isCreatedBeforeCutoff(resolvePayMethod(candidate.payMethod()), candidate.createdAt(), now);
    }

    public boolean isEligible(Order order, LocalDateTime now) {
        if (!archiveProperties.getArchivableStatuses().contains(order.getStatus())) {
            return false;
        }
        return isCreatedBeforeCutoff(resolvePayMethod(order.getPayMethod()), order.getCreatedAt(), now);
    }

    private boolean isCreatedBeforeCutoff(String payMethod, LocalDateTime createdAt, LocalDateTime now) {
        if (createdAt == null) {
            return false;
        }
        if (normalizeAll(archiveProperties.getImmediatePayMethods()).contains(payMethod)) {
            return !createdAt.isAfter(now.minus(archiveProperties.getImmediateRetention()));
        }
        if (normalizeAll(archiveProperties.getDelayedPayMethods()).contains(payMethod)) {
            return !createdAt.isAfter(now.minus(archiveProperties.getDelayedRetention()));
        }
        return !createdAt.isAfter(now.minus(archiveProperties.getUnknownRetention()));
    }

    private String resolvePayMethod(String payMethod) {
        if (StringUtils.hasText(payMethod)) {
            return normalize(payMethod);
        }
        return defaultPayMethod();
    }

    private String defaultPayMethod() {
        String payMethod = payMethodResolver.resolve();
        return StringUtils.hasText(payMethod) ? normalize(payMethod) : UNKNOWN_METHOD;
    }

    static String normalize(String payMethod) {
        return payMethod.trim().toUpperCase(Locale.ROOT);
    }

    private static Set<String> normalizeAll(Iterable<String> payMethods) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String payMethod : payMethods) {
            if (StringUtils.hasText(payMethod)) {
                normalized.add(normalize(payMethod));
            }
        }
        return normalized.stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
