package com.ecommerce.paymentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment.stripe")
@Data
public class StripeConfig {
    private String apiKey;
    private String webhookSecret;
}

