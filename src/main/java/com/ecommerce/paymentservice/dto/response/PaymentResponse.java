package com.ecommerce.paymentservice.dto.response;

import com.ecommerce.paymentservice.enums.PaymentMethod;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private String paymentNumber;
    private String orderId;
    private String orderNumber;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String vnpayTransactionNo;
    private String transactionId;
    private String paymentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}

