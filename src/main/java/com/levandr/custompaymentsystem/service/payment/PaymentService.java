package com.levandr.custompaymentsystem.service.payment;

import com.levandr.custompaymentsystem.entity.Payment;
import com.levandr.custompaymentsystem.repository.PaymentEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public Payment createPayment(String recordNumber, String paymentId,
                                 String companyName, String payerINN,
                                 BigDecimal amount, Integer status,
                                 String file_name){

        Payment payment = new Payment(recordNumber, paymentId, companyName, payerINN, amount, file_name);
        payment.setStatusCode(status);

        return paymentEntityRepository.save(payment);
    }
}
