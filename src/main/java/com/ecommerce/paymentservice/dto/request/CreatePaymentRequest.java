package com.ecommerce.paymentservice.dto.request;

import com.ecommerce.paymentservice.enums.PaymentMethod;
import com.ecommerce.paymentservice.enums.PaymentProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod method;
    private PaymentProvider provider;
    private String metadata;
}

