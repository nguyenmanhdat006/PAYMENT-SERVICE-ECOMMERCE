package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request,
                                                         HttpServletRequest httpRequest) {
        log.info("Creating payment for orderNumber={}", request.getOrderNumber());
        String ipAddress = getClientIp(httpRequest);
        return ResponseEntity.ok(paymentService.createPayment(request, ipAddress));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        log.info("Getting payment id={}", id);
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(@PathVariable String orderId) {
        log.info("Getting payment by orderId={}", orderId);
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @PostMapping("/{paymentNumber}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable String paymentNumber) {
        log.info("Confirming paymentNumber={}", paymentNumber);
        return ResponseEntity.ok(paymentService.confirmPayment(paymentNumber));
    }

    @PutMapping("/order/{orderId}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatusForOrder(
            @PathVariable String orderId,
            @RequestParam String status) {
        log.info("Updating payment status for orderId={}, newStatus={}", orderId, status);
        return ResponseEntity.ok(paymentService.updatePaymentStatus(orderId, status));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            return ip.split(",")[0].trim();
        }
        return ip;
    }
}

