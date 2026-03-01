package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.response.ApiResponse;
import com.ecommerce.paymentservice.enums.PaymentProvider;
import com.ecommerce.paymentservice.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@Slf4j
public class WebhookController {

    private final PaymentService paymentService;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<ApiResponse<String>> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        log.info("Received Stripe webhook");

        try {
            paymentService.handleWebhook(PaymentProvider.STRIPE, payload, signature);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .code(200)
                    .message("Webhook processed successfully")
                    .success(true)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing Stripe webhook: {}", e.getMessage());

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .code(400)
                    .message("Error processing webhook: " + e.getMessage())
                    .success(false)
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/vnpay")
    public ResponseEntity<ApiResponse<String>> vnpayWebhook(
            @RequestBody String payload,
            @RequestHeader("VNPay-Signature") String signature) {
        log.info("Received VNPay webhook");

        try {
            paymentService.handleWebhook(PaymentProvider.VNPAY, payload, signature);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .code(200)
                    .message("Webhook processed successfully")
                    .success(true)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing VNPay webhook: {}", e.getMessage());

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .code(400)
                    .message("Error processing webhook: " + e.getMessage())
                    .success(false)
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }
}

