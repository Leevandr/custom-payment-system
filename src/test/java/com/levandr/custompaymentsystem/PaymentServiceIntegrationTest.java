package com.levandr.custompaymentsystem;

import com.levandr.custompaymentsystem.entity.PaymentEntity;
import com.levandr.custompaymentsystem.entity.PaymentStatus;
import com.levandr.custompaymentsystem.repository.PaymentEntityRepository;
import com.levandr.custompaymentsystem.service.payment.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentEntityRepository paymentEntityRepository;

    @Test
    public void testCreateAndFindPayment() {
        PaymentEntity paymentEntity = paymentService.createPayment(
                "000001000001",
                "123456789-123456789-123456789-123356789-1234567890",
                "Наименование",
                "12345678912",
                new BigDecimal("100000000.12"),
                PaymentStatus.OK.getCode(),
                "test_file"
        );

        assertThat(paymentEntity.getId()).isNotNull();

        Optional<PaymentEntity> foundPayment = paymentService.findPaymentByPaymentId(paymentEntity.getPaymentId());

        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get().getPaymentId()).isEqualTo(paymentEntity.getPaymentId());

    }
}
