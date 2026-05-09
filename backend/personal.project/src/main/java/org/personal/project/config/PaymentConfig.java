package org.personal.project.config;

import org.personal.project.properties.PortOnePaymentProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Clock;

@Configuration
public class PaymentConfig {

    @Bean
    public RestClient portOneRestClient(RestClient.Builder builder, PortOnePaymentProperties properties) {
        return builder
                .baseUrl(properties.getApiBaseUrl())
                .defaultHeader("Authorization", "PortOne " + properties.getApiSecret())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
