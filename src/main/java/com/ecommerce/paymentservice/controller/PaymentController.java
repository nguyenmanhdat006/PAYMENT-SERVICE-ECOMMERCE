package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.request.ConfirmPaymentRequest;
import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.paymentservice.dto.request.RefundRequest;
import com.ecommerce.paymentservice.dto.response.ApiResponse;
import com.ecommerce.paymentservice.dto.response.PaymentIntentResponse;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentIntentResponse>> createPayment(
            @RequestBody CreatePaymentRequest request) {
        log.info("Creating payment for orderId: {}", request.getOrderId());

        PaymentIntentResponse response = paymentService.createPaymentIntent(request);

        ApiResponse<PaymentIntentResponse> apiResponse = ApiResponse.<PaymentIntentResponse>builder()
                .code(201)
                .message("Payment intent created successfully")
                .data(response)
                .success(true)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(
            @PathVariable String id,
            @RequestBody ConfirmPaymentRequest request) {
        log.info("Confirming payment: {}", id);

        PaymentResponse response = paymentService.confirmPayment(id, request);

        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Payment confirmed successfully")
                .data(response)
                .success(true)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable String id,
            @RequestBody RefundRequest request) {
        log.info("Refunding payment: {}", id);

        PaymentResponse response = paymentService.refundPayment(id, request);

        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Payment refunded successfully")
                .data(response)
                .success(true)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable String id) {
        log.info("Fetching payment: {}", id);

        PaymentResponse response = paymentService.getPaymentById(id);

        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Payment retrieved successfully")
                .data(response)
                .success(true)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(
            @PathVariable String orderId) {
        log.info("Fetching payment by orderId: {}", orderId);

        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);

        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Payment retrieved successfully")
                .data(response)
                .success(true)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}

