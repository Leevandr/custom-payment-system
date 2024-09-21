package com.levandr.custompaymentsystem.service.payment;

import com.levandr.custompaymentsystem.entity.PaymentEntity;
import com.levandr.custompaymentsystem.repository.PaymentEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentEntityRepository paymentEntityRepository;

    public void savePayment(PaymentEntity paymentEntity) {
        paymentEntityRepository.save(paymentEntity);
    }

    public Optional<PaymentEntity> findPaymentId(Long id) {
        return paymentEntityRepository.findById(id);
    }

    public Optional<PaymentEntity> findPaymentByPaymentId(String paymentId) {
        return paymentEntityRepository.findByPaymentId(paymentId);
    }

    public PaymentEntity createPayment(String recordNumber, String paymentId,
                                       String companyName, String payerINN,
                                       BigDecimal amount, Integer status,
                                       String file_name){

        PaymentEntity paymentEntity = new PaymentEntity(recordNumber, paymentId, companyName, payerINN, amount, file_name);
        paymentEntity.setStatus(status);

        return paymentEntityRepository.save(paymentEntity);
    }
}
