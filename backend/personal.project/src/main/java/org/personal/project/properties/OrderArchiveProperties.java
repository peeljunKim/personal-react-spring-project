package org.personal.project.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.personal.project.entity.OrderStatus;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "order.archive")
public class OrderArchiveProperties {

    private boolean enabled;

    @NotBlank
    private String cron;

    @NotBlank
    private String zoneId;

    @NotBlank
    private String storagePath;

    @Min(1)
    private int pageSize;

    @Min(1)
    private int maxPagesPerRun;

    @NotNull
    private Duration immediateRetention;

    @NotNull
    private Duration delayedRetention;

    @NotNull
    private Duration unknownRetention;

    @NotNull
    private Duration lockWait;

    @NotNull
    private Duration lockLease;

    @NotEmpty
    private List<String> immediatePayMethods;

    @NotEmpty
    private List<String> delayedPayMethods;

    @NotEmpty
    private List<OrderStatus> archivableStatuses;
}
