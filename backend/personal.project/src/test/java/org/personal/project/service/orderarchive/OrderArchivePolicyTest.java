package org.personal.project.service.orderarchive;

import org.junit.jupiter.api.Test;
import org.personal.project.entity.OrderStatus;
import org.personal.project.properties.OrderArchiveProperties;
import org.personal.project.properties.PortOnePaymentProperties;
import org.personal.project.service.payment.PortOnePayMethodResolver;
import org.springframework.mock.env.MockEnvironment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderArchivePolicyTest {

    private static final LocalDateTime NOW = LocalDateTime.parse("2026-05-14T12:00:00");

    @Test
    void immediatePaymentMethodsUseShortRetention() {
        OrderArchivePolicy policy = policy("EASY_PAY");

        assertTrue(policy.isEligible(candidate("CARD", NOW.minusMinutes(31), OrderStatus.READY), NOW));
        assertFalse(policy.isEligible(candidate("CARD", NOW.minusMinutes(29), OrderStatus.READY), NOW));
    }

    @Test
    void delayedPaymentMethodsUseLongRetention() {
        OrderArchivePolicy policy = policy("EASY_PAY");

        assertTrue(policy.isEligible(candidate("VBANK", NOW.minusHours(73), OrderStatus.READY), NOW));
        assertFalse(policy.isEligible(candidate("VBANK", NOW.minusHours(48), OrderStatus.READY), NOW));
    }

    @Test
    void paidOrdersAreNeverArchived() {
        OrderArchivePolicy policy = policy("EASY_PAY");

        assertFalse(policy.isEligible(candidate("CARD", NOW.minusDays(30), OrderStatus.PAID), NOW));
    }

    @Test
    void nullPayMethodUsesConfiguredDefaultMethod() {
        OrderArchivePolicy policy = policy("CARD");

        assertTrue(policy.isEligible(candidate(null, NOW.minusMinutes(31), OrderStatus.READY), NOW));
    }

    private OrderArchivePolicy policy(String defaultPayMethod) {
        OrderArchiveProperties archiveProperties = new OrderArchiveProperties();
        archiveProperties.setImmediateRetention(Duration.ofMinutes(30));
        archiveProperties.setDelayedRetention(Duration.ofHours(72));
        archiveProperties.setUnknownRetention(Duration.ofHours(72));
        archiveProperties.setImmediatePayMethods(List.of("EASY_PAY", "CARD", "TRANSFER", "TRANS", "PHONE"));
        archiveProperties.setDelayedPayMethods(List.of("VBANK", "VIRTUAL_ACCOUNT"));
        archiveProperties.setArchivableStatuses(List.of(OrderStatus.READY, OrderStatus.CANCEL));

        PortOnePaymentProperties paymentProperties = new PortOnePaymentProperties();
        paymentProperties.setPayMethod(defaultPayMethod);
        return new OrderArchivePolicy(
                archiveProperties,
                new PortOnePayMethodResolver(paymentProperties, new MockEnvironment())
        );
    }

    private OrderArchiveCandidate candidate(String payMethod, LocalDateTime createdAt, OrderStatus status) {
        return new OrderArchiveCandidate(1L, "payment-1", status, payMethod, createdAt);
    }
}
