package org.personal.project.service.payment;

import org.junit.jupiter.api.Test;
import org.personal.project.properties.PortOnePaymentProperties;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortOnePayMethodResolverTest {

    @Test
    void usesEasyPayByDefaultOutsideProd() {
        PortOnePayMethodResolver resolver = new PortOnePayMethodResolver(
                new PortOnePaymentProperties(),
                new MockEnvironment()
        );

        assertEquals("EASY_PAY", resolver.resolve());
    }

    @Test
    void usesCardByDefaultInProdProfile() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        PortOnePayMethodResolver resolver = new PortOnePayMethodResolver(
                new PortOnePaymentProperties(),
                environment
        );

        assertEquals("CARD", resolver.resolve());
    }

    @Test
    void externalPayMethodOverridesProfileDefault() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        PortOnePaymentProperties properties = new PortOnePaymentProperties();
        properties.setPayMethod("easy_pay");
        PortOnePayMethodResolver resolver = new PortOnePayMethodResolver(properties, environment);

        assertEquals("EASY_PAY", resolver.resolve());
    }
}
