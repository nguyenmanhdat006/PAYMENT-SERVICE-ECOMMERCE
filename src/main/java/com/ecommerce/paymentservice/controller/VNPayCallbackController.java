package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import com.ecommerce.paymentservice.service.PaymentService;
import com.ecommerce.paymentservice.service.VNPayPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class VNPayCallbackController {

    private final VNPayPaymentService vnPayPaymentService;
    private final PaymentService paymentService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("/payments/vnpay/callback")
    public RedirectView vnpayCallback(@RequestParam Map<String, String> params) {
        log.info("Received VNPay callback");

        try {
            Payment payment = vnPayPaymentService.processCallback(params);
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                paymentService.markPaymentSuccess(payment.getOrderNumber(), payment.getTransactionId());
            }

            return new RedirectView(buildRedirectUrl(payment));
        } catch (Exception e) {
            log.error("Error processing VNPay callback", e);
            String errorUrl = frontendUrl + "/payment/error?message=" +
                    URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return new RedirectView(errorUrl);
        }
    }

    private String buildRedirectUrl(Payment payment) {
        StringBuilder url = new StringBuilder(frontendUrl);
        url.append("/payment/result");
        url.append("?orderId=").append(urlEncode(payment.getOrderId()));
        url.append("&status=").append(payment.getStatus());
        url.append("&paymentNumber=").append(urlEncode(payment.getPaymentNumber()));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            url.append("&transactionId=").append(urlEncode(payment.getTransactionId() != null ? payment.getTransactionId() : ""));
        } else if (payment.getStatus() == PaymentStatus.FAILED) {
            url.append("&reason=").append(urlEncode(payment.getFailureReason() != null ? payment.getFailureReason() : "Unknown"));
        }

        return url.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

