package com.ecommerce.paymentservice.dto.response;

import com.ecommerce.paymentservice.enums.PaymentStatus;
import com.ecommerce.paymentservice.enums.PaymentProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private PaymentProvider provider;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

