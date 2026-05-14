package org.personal.project.service.payment;

import org.personal.project.properties.PortOnePaymentProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Locale;

@Component
public class PortOnePayMethodResolver {

    private static final String PROD_PROFILE = "prod";
    private static final String PROD_DEFAULT_PAY_METHOD = "CARD";
    private static final String DEFAULT_PAY_METHOD = "EASY_PAY";

    private final PortOnePaymentProperties properties;
    private final Environment environment;

    public PortOnePayMethodResolver(PortOnePaymentProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    public String resolve() {
        if (StringUtils.hasText(properties.getPayMethod())) {
            return normalize(properties.getPayMethod());
        }
        if (Arrays.asList(environment.getActiveProfiles()).contains(PROD_PROFILE)) {
            return PROD_DEFAULT_PAY_METHOD;
        }
        return DEFAULT_PAY_METHOD;
    }

    private String normalize(String payMethod) {
        return payMethod.trim().toUpperCase(Locale.ROOT);
    }
}
