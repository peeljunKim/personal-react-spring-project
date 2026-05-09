package org.personal.project.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "portone")
public class PortOnePaymentProperties {

    private String apiBaseUrl;
    private String apiSecret;
    private String storeId;
    private String channelKey;
    private String webhookUrl;
    private String webhookSecret;
    private boolean webhookSignatureRequired = true;
}
