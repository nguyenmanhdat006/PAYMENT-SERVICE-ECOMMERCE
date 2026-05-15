package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.config.VNPayConfig;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import com.ecommerce.paymentservice.exception.InvalidSignatureException;
import com.ecommerce.paymentservice.exception.PaymentNotFoundException;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.ecommerce.paymentservice.util.VNPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class VNPayPaymentService {

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;

    public VNPayPaymentService(VNPayConfig vnPayConfig, PaymentRepository paymentRepository) {
        this.vnPayConfig = vnPayConfig;
        this.paymentRepository = paymentRepository;
    }

    public String createPaymentUrl(Payment payment, String ipAddress) {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnPayConfig.getVersion());
        params.put("vnp_Command", vnPayConfig.getCommand());
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_Amount", payment.getAmount().multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", payment.getPaymentNumber());
        params.put("vnp_OrderInfo", payment.getDescription() != null ? payment.getDescription() : "Thanh toan don hang " + payment.getOrderNumber());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        params.put("vnp_IpAddr", ipAddress);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        params.put("vnp_CreateDate", now.format(formatter));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(formatter));

        String queryString = VNPayUtil.buildQueryString(params);
        String secureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), queryString);

        String paymentUrl = vnPayConfig.getVnpayUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;
        log.info("Created VNPay URL for paymentNumber={}", payment.getPaymentNumber());
        return paymentUrl;
    }

    public Payment processCallback(Map<String, String> callbackParams) {
        Map<String, String> params = new LinkedHashMap<>(callbackParams);
        String providedSignature = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String signData = VNPayUtil.buildQueryString(params);
        String calculatedSignature = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), signData);

        if (providedSignature == null || !providedSignature.equalsIgnoreCase(calculatedSignature)) {
            throw new InvalidSignatureException("Invalid VNPay callback signature");
        }

        String paymentNumber = params.get("vnp_TxnRef");
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentNumber));

        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setVnpayTransactionNo(params.get("vnp_TransactionNo"));
            payment.setVnpayBankCode(params.get("vnp_BankCode"));
            payment.setVnpayCardType(params.get("vnp_CardType"));
            payment.setTransactionId(params.get("vnp_TransactionNo"));
            payment.setPaidAt(LocalDateTime.now());
            payment.setFailureReason(null);
            log.info("VNPay callback SUCCESS for paymentNumber={}", paymentNumber);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("VNPay response code: " + responseCode);
            log.warn("VNPay callback FAILED for paymentNumber={}, code={}", paymentNumber, responseCode);
        }

        return paymentRepository.save(payment);
    }
}

