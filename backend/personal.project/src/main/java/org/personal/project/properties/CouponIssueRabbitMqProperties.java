package org.personal.project.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "coupon.issue.rabbitmq")
public class CouponIssueRabbitMqProperties {

    private String exchange;
    private String queue;
    private String routingKey;
    private String deadLetterExchange;
    private String deadLetterQueue;
    private String deadLetterRoutingKey;
    private int prefetch;
}
