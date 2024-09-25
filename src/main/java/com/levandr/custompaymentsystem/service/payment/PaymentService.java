package com.levandr.custompaymentsystem.service.payment;

import com.levandr.custompaymentsystem.entity.Payment;
import com.levandr.custompaymentsystem.repository.PaymentEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentEntityRepository paymentEntityRepository;

    public void savePayment(Payment payment) {
        paymentEntityRepository.save(payment);
    }

    public Optional<Payment> findPaymentId(Long id) {
        return paymentEntityRepository.findById(id);
    }

    public Optional<Payment> findPaymentByPaymentId(String paymentId) {
        return paymentEntityRepository.findByPaymentId(paymentId);
    }

    public Payment createPayment(String paymentId, String recordNumber,
                                 String companyName, String payerInn,
                                 BigDecimal amount, Integer status,
                                 String fileName) {

        Payment payment = new Payment(null, paymentId, recordNumber, companyName, payerInn, amount, status, fileName);
        return paymentEntityRepository.save(payment);
    }

    public void saveAll(List<Payment> payments) {
        paymentEntityRepository.saveAll(payments);
    }
}
