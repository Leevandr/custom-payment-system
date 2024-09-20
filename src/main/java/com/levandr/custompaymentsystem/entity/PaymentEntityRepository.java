package com.levandr.custompaymentsystem.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentEntityRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByPaymentId(String paymentId);
}