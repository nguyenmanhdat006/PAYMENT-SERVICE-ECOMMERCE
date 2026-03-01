package com.ecommerce.paymentservice.repository;

import com.ecommerce.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByUserId(String userId);

    Optional<Payment> findByTransactionId(String transactionId);
}

