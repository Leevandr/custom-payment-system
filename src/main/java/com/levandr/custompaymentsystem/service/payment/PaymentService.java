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

    /**
     * Сохраняет платеж и возвращает сохраненный объект.
     *
     * @param payment объект платежа для сохранения.
     * @return сохраненный объект Payment.
     */
    public Payment savePayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment entity cannot be null");
        }
        return paymentEntityRepository.save(payment);
    }

    /**
     * Находит платеж по уникальному идентификатору.
     *
     * @param id уникальный идентификатор записи.
     * @return Optional с объектом Payment, если найден.
     */
    public Optional<Payment> findPaymentId(Long id) {
        return paymentEntityRepository.findById(id);
    }

    /**
     * Находит платеж по идентификатору платежа.
     *
     * @param paymentId уникальный идентификатор платежа.
     * @return Optional с объектом Payment, если найден.
     */
    public Optional<Payment> findPaymentByPaymentId(String paymentId) {
        return paymentEntityRepository.findByPaymentId(paymentId);
    }

    /**
     * Создает новый платеж и сохраняет его в базе данных.
     *
     * @param paymentId    уникальный идентификатор платежа.
     * @param recordNumber номер записи.
     * @param companyName  название компании.
     * @param payerInn     ИНН плательщика.
     * @param amount       сумма платежа.
     * @param status       статус платежа.
     * @param fileName     имя файла, из которого получены данные.
     * @return сохраненный объект Payment.
     */
    public Payment createPayment(String paymentId, String recordNumber,
                                 String companyName, String payerInn,
                                 BigDecimal amount, Integer status,
                                 String fileName) {

        Payment payment = new Payment(null,
                paymentId, recordNumber, companyName, payerInn, amount, status, fileName);
        return paymentEntityRepository.save(payment);
    }

    /**
     * Сохраняет список платежей.
     *
     * @param payments список платежей для сохранения.
     */
    public List<Payment> saveAll(List<Payment> payments) {
        return paymentEntityRepository.saveAll(payments);
    }
}
