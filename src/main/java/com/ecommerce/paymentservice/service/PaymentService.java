package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.request.ConfirmPaymentRequest;
import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.paymentservice.dto.request.RefundRequest;
import com.ecommerce.paymentservice.dto.response.PaymentIntentResponse;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.entity.PaymentTransaction;
import com.ecommerce.paymentservice.enums.PaymentProvider;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import com.ecommerce.paymentservice.exception.PaymentException;
import com.ecommerce.paymentservice.mapper.PaymentMapper;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.ecommerce.paymentservice.repository.PaymentTransactionRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final StripePaymentService stripePaymentService;
    private final PaymentMapper paymentMapper;

    public PaymentService(PaymentRepository paymentRepository,
                         PaymentTransactionRepository paymentTransactionRepository,
                         StripePaymentService stripePaymentService,
                         PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.stripePaymentService = stripePaymentService;
        this.paymentMapper = paymentMapper;
    }

    public PaymentIntentResponse createPaymentIntent(CreatePaymentRequest request) {
        try {
            // Validate request
            if (request.getAmount() == null || request.getAmount().signum() <= 0) {
                throw new PaymentException("Amount must be greater than 0", "INVALID_AMOUNT", 400);
            }

            if (request.getProvider() == null) {
                throw new PaymentException("Payment provider is required", "INVALID_PROVIDER", 400);
            }

            // Create payment intent with provider
            PaymentIntentResponse intentResponse = null;

            if (request.getProvider() == PaymentProvider.STRIPE) {
                intentResponse = stripePaymentService.createPaymentIntent(
                        request.getAmount(),
                        request.getCurrency() != null ? request.getCurrency() : "USD",
                        request.getOrderId()
                );
            } else {
                throw new PaymentException("Unsupported payment provider", "UNSUPPORTED_PROVIDER", 400);
            }

            // Save payment record
            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                    .method(request.getMethod())
                    .status(PaymentStatus.PENDING)
                    .provider(request.getProvider())
                    .transactionId(intentResponse.getPaymentIntentId())
                    .metadata(request.getMetadata())
                    .build();

            paymentRepository.save(payment);

            log.info("Payment intent created for orderId: {}", request.getOrderId());
            return intentResponse;
        } catch (PaymentException e) {
            log.error("Payment exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating payment intent: {}", e.getMessage());
            throw new PaymentException("Failed to create payment intent", "INTERNAL_ERROR", 500);
        }
    }

    public PaymentResponse confirmPayment(String paymentId, ConfirmPaymentRequest request) {
        try {
            UUID id = UUID.fromString(paymentId);
            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> new PaymentException("Payment not found", "PAYMENT_NOT_FOUND", 404));

            // Confirm with payment provider
            PaymentIntent paymentIntent = null;

            if (payment.getProvider() == PaymentProvider.STRIPE) {
                paymentIntent = stripePaymentService.confirmPayment(request.getPaymentIntentId());
            }

            // Update payment status based on provider response
            if (paymentIntent != null) {
                String status = paymentIntent.getStatus();
                if ("succeeded".equals(status)) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                } else if ("processing".equals(status)) {
                    payment.setStatus(PaymentStatus.PROCESSING);
                } else if ("requires_action".equals(status) || "requires_payment_method".equals(status)) {
                    payment.setStatus(PaymentStatus.PENDING);
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                }
            }

            // Save transaction record
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .payment(payment)
                    .transactionCode(request.getPaymentIntentId())
                    .status(payment.getStatus())
                    .amount(payment.getAmount())
                    .description("Payment confirmation")
                    .response(paymentIntent != null ? paymentIntent.toJson().toString() : "")
                    .build();

            paymentTransactionRepository.save(transaction);
            paymentRepository.save(payment);

            log.info("Payment confirmed for paymentId: {}", paymentId);
            return paymentMapper.toResponse(payment);
        } catch (PaymentException e) {
            log.error("Payment exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error confirming payment: {}", e.getMessage());
            throw new PaymentException("Failed to confirm payment", "INTERNAL_ERROR", 500);
        }
    }

    public PaymentResponse refundPayment(String paymentId, RefundRequest request) {
        try {
            UUID id = UUID.fromString(paymentId);
            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> new PaymentException("Payment not found", "PAYMENT_NOT_FOUND", 404));

            if (payment.getStatus() != PaymentStatus.COMPLETED) {
                throw new PaymentException("Can only refund completed payments", "INVALID_PAYMENT_STATUS", 400);
            }

            // Process refund with provider
            if (payment.getProvider() == PaymentProvider.STRIPE) {
                Refund refund = null;
                if (request.getAmount() != null) {
                    refund = stripePaymentService.refundPayment(payment.getTransactionId(), request.getAmount());
                } else {
                    refund = stripePaymentService.refundPayment(payment.getTransactionId());
                }

                if (refund != null) {
                    if ("succeeded".equals(refund.getStatus())) {
                        payment.setStatus(PaymentStatus.REFUNDED);
                    } else {
                        payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
                    }
                }
            }

            // Save transaction record
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .payment(payment)
                    .transactionCode(payment.getTransactionId())
                    .status(payment.getStatus())
                    .amount(request.getAmount() != null ? request.getAmount() : payment.getAmount())
                    .description("Refund: " + request.getReason())
                    .build();

            paymentTransactionRepository.save(transaction);
            paymentRepository.save(payment);

            log.info("Payment refunded for paymentId: {}", paymentId);
            return paymentMapper.toResponse(payment);
        } catch (PaymentException e) {
            log.error("Payment exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error refunding payment: {}", e.getMessage());
            throw new PaymentException("Failed to refund payment", "INTERNAL_ERROR", 500);
        }
    }

    public PaymentResponse getPaymentById(String paymentId) {
        try {
            UUID id = UUID.fromString(paymentId);
            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> new PaymentException("Payment not found", "PAYMENT_NOT_FOUND", 404));

            return paymentMapper.toResponse(payment);
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching payment: {}", e.getMessage());
            throw new PaymentException("Failed to fetch payment", "INTERNAL_ERROR", 500);
        }
    }

    public PaymentResponse getPaymentByOrderId(String orderId) {
        try {
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new PaymentException("Payment not found for order", "PAYMENT_NOT_FOUND", 404));

            return paymentMapper.toResponse(payment);
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching payment by orderId: {}", e.getMessage());
            throw new PaymentException("Failed to fetch payment", "INTERNAL_ERROR", 500);
        }
    }

    public void handleWebhook(PaymentProvider provider, String payload, String signature) {
        try {
            if (provider == PaymentProvider.STRIPE) {
                if (!stripePaymentService.verifyWebhookSignature(payload, signature)) {
                    throw new PaymentException("Invalid webhook signature", "INVALID_SIGNATURE", 401);
                }

                // Parse and process webhook event
                log.info("Valid Stripe webhook received");
            }
        } catch (PaymentException e) {
            log.error("Webhook processing error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing webhook: {}", e.getMessage());
            throw new PaymentException("Failed to process webhook", "INTERNAL_ERROR", 500);
        }
    }
}

