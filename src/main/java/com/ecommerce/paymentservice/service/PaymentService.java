package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.enums.PaymentMethod;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import com.ecommerce.paymentservice.exception.PaymentNotFoundException;
import com.ecommerce.paymentservice.mapper.PaymentMapper;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final VNPayPaymentService vnPayPaymentService;
    private final WebClient orderServiceClient;
    private final PaymentMapper paymentMapper;

    public PaymentService(PaymentRepository paymentRepository,
                         VNPayPaymentService vnPayPaymentService,
                         WebClient orderServiceClient,
                         PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.vnPayPaymentService = vnPayPaymentService;
        this.orderServiceClient = orderServiceClient;
        this.paymentMapper = paymentMapper;
    }

    public PaymentResponse createPayment(CreatePaymentRequest request, String ipAddress) {
        Payment payment = Payment.builder()
                .paymentNumber(generatePaymentNumber())
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency("VND")
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Created payment: paymentNumber={}, method={}", savedPayment.getPaymentNumber(), savedPayment.getPaymentMethod());

        if (savedPayment.getPaymentMethod() == PaymentMethod.VNPAY) {
            String paymentUrl = vnPayPaymentService.createPaymentUrl(savedPayment, ipAddress);
            return paymentMapper.toResponse(savedPayment, paymentUrl);
        }

        return paymentMapper.toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderNumber(String orderNumber) {
        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for orderNumber: " + orderNumber));
        return paymentMapper.toResponse(payment);
    }

    public PaymentResponse confirmPayment(String paymentNumber) {
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with paymentNumber: " + paymentNumber));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);
        }

        return markPaymentSuccess(payment.getOrderNumber(), payment.getTransactionId());
    }

    public PaymentResponse markPaymentSuccess(String orderNumber, String transactionId) {
        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for orderNumber: " + orderNumber));

        payment.setStatus(PaymentStatus.SUCCESS);
        if (transactionId != null && !transactionId.isBlank()) {
            payment.setTransactionId(transactionId);
            payment.setVnpayTransactionNo(transactionId);
        }
        payment.setPaidAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentNumber", savedPayment.getPaymentNumber());
        requestBody.put("transactionId", savedPayment.getTransactionId());

        try {
            orderServiceClient.put()
                    .uri("/api/orders/{orderId}/payment-confirmed", savedPayment.getOrderId())
                    .bodyValue(requestBody)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Notified order-service for successful payment: orderNumber={}, paymentNumber={}",
                    orderNumber, savedPayment.getPaymentNumber());
        } catch (WebClientResponseException e) {
            log.error("Order service callback failed: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Order service callback failed for orderNumber={}: {}", orderNumber, e.getMessage());
        }

        return paymentMapper.toResponse(savedPayment);
    }

    private String generatePaymentNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomPart;
        String paymentNumber;
        do {
            randomPart = com.ecommerce.paymentservice.util.VNPayUtil.getRandomNumber(4);
            paymentNumber = "PAY-" + datePart + "-" + randomPart;
        } while (paymentRepository.findByPaymentNumber(paymentNumber).isPresent());

        return paymentNumber;
    }
}

