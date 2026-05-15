package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@Slf4j
public class WebhookController {

    @PostMapping("/stripe")
    public ResponseEntity<ApiResponse<String>> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        log.warn("Stripe webhook endpoint is deprecated. payloadSize={}, signaturePresent={}",
                payload != null ? payload.length() : 0,
                signature != null && !signature.isBlank());

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(410)
                .message("Stripe webhook is not supported in COD/VNPay mode")
                .success(false)
                .build();
        return ResponseEntity.status(410).body(response);
    }

    @PostMapping("/vnpay")
    public ResponseEntity<ApiResponse<String>> vnpayWebhook(
            @RequestBody String payload,
            @RequestHeader("VNPay-Signature") String signature) {
        log.warn("VNPay webhook endpoint is deprecated. payloadSize={}, signaturePresent={}",
                payload != null ? payload.length() : 0,
                signature != null && !signature.isBlank());

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(410)
                .message("Use GET /api/payments/vnpay/callback for VNPay callback")
                .success(false)
                .build();
        return ResponseEntity.status(410).body(response);
    }
}

