package com.ecommerce.paymentservice.repository;

import com.ecommerce.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentNumber(String paymentNumber);

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByOrderNumber(String orderNumber);

    List<Payment> findByUserId(String userId);

    Optional<Payment> findByTransactionId(String transactionId);
}

