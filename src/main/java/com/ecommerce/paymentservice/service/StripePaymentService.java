package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.config.StripeConfig;
import com.ecommerce.paymentservice.dto.response.PaymentIntentResponse;
import com.ecommerce.paymentservice.exception.PaymentException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StripePaymentService {

    private final StripeConfig stripeConfig;

    public StripePaymentService(StripeConfig stripeConfig) {
        this.stripeConfig = stripeConfig;
        Stripe.apiKey = stripeConfig.getApiKey();
    }

    public PaymentIntentResponse createPaymentIntent(BigDecimal amount, String currency, String orderId) {
        try {
            long amountCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency(currency.toLowerCase())
                    .putMetadata("orderId", orderId)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            return PaymentIntentResponse.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .build();
        } catch (StripeException e) {
            log.error("Error creating payment intent: {}", e.getMessage());
            throw new PaymentException("Failed to create payment intent", "STRIPE_ERROR", 400);
        }
    }

    public PaymentIntent confirmPayment(String paymentIntentId) {
        try {
            return PaymentIntent.retrieve(paymentIntentId);
        } catch (StripeException e) {
            log.error("Error confirming payment: {}", e.getMessage());
            throw new PaymentException("Failed to confirm payment", "STRIPE_ERROR", 400);
        }
    }

    public Refund refundPayment(String paymentIntentId, BigDecimal amount) {
        try {
            long amountCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .setAmount(amountCents)
                    .build();

            return Refund.create(params);
        } catch (StripeException e) {
            log.error("Error refunding payment: {}", e.getMessage());
            throw new PaymentException("Failed to refund payment", "STRIPE_ERROR", 400);
        }
    }

    public Refund refundPayment(String paymentIntentId) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();

            return Refund.create(params);
        } catch (StripeException e) {
            log.error("Error refunding payment: {}", e.getMessage());
            throw new PaymentException("Failed to refund payment", "STRIPE_ERROR", 400);
        }
    }

    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            com.stripe.net.Webhook.constructEvent(
                    payload,
                    signature,
                    stripeConfig.getWebhookSecret()
            );
            return true;
        } catch (Exception e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}

