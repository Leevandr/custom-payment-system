package com.levandr.custompaymentsystem.service.payment;

import com.levandr.custompaymentsystem.entity.Payment;
import com.levandr.custompaymentsystem.repository.PaymentEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentEntityRepository paymentEntityRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setPaymentId("12345");
        payment.setRecordNumber("001");
        payment.setCompanyName("Test Company");
        payment.setPayerInn("123456789");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatusCode(1);
        payment.setFileName("test-file.txt");
    }

    @Test
    void testSavePaymentThrowsExceptionWhenPaymentIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.savePayment(null);
        });
        assertEquals("Payment entity cannot be null", exception.getMessage());
    }

    @Test
    void testSavePaymentSuccessfully() {
        when(paymentEntityRepository.save(payment)).thenReturn(payment);

        Payment savedPayment = paymentService.savePayment(payment);

        assertNotNull(savedPayment);
        assertEquals(payment.getPaymentId(), savedPayment.getPaymentId());

        verify(paymentEntityRepository, times(1)).save(payment);
    }

    @Test
    void testFindPaymentById() {
        Long paymentId = 1L;
        Payment payment = new Payment(paymentId, "payment123", "record123", "Company", "123456789", new BigDecimal("100.00"), 1, "file.txt");

        when(paymentEntityRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // Вызов метода сервиса
        Optional<Payment> result = paymentService.findPaymentId(paymentId);

        // Проверка, что результат присутствует и id соответствует ожидаемому
        assertTrue(result.isPresent());
        assertEquals(paymentId, result.get().getId());
    }

    @Test
    void testFindPaymentByPaymentId() {
        String paymentId = "12345";
        when(paymentEntityRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(payment));

        Optional<Payment> result = paymentService.findPaymentByPaymentId(paymentId);

        assertTrue(result.isPresent());
        assertEquals(paymentId, result.get().getPaymentId());
    }
    @Test
    void testSavePaymentWithNull() {
        // Проверка, что при попытке сохранения null будет выброшено исключение
        assertThrows(IllegalArgumentException.class, () -> paymentService.savePayment(null));
    }
    @Test
    void testSavePaymentWithInvalidData() {
        Payment invalidPayment = new Payment(null, null, null, null, null, null, null, null);

        // Мокаем поведение репозитория, чтобы исключение не выбрасывалось
        when(paymentEntityRepository.save(any(Payment.class))).thenThrow(new IllegalArgumentException("Invalid payment data"));

        assertThrows(IllegalArgumentException.class, () -> paymentService.savePayment(invalidPayment));
    }
    @Test
    void testSaveMultiplePayments() {
        Payment payment1 = new Payment(1L, "payment123", "record123", "Company1", "123456789", new BigDecimal("100.00"), 1, "file1.txt");
        Payment payment2 = new Payment(2L, "payment124", "record124", "Company2", "987654321", new BigDecimal("200.00"), 1, "file2.txt");
        List<Payment> payments = Arrays.asList(payment1, payment2);

        when(paymentEntityRepository.saveAll(payments)).thenReturn(payments);

        List<Payment> savedPayments = paymentService.saveAll(payments);

        assertEquals(2, savedPayments.size());
        assertTrue(savedPayments.contains(payment1));
        assertTrue(savedPayments.contains(payment2));
    }

    @Test
    void testSaveManyPayments() {
        // Подготовка большого числа платежей
        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            payments.add(new Payment((long) i, "payment" + i, "record" + i, "Company" + i, "123456789", new BigDecimal("100.00"), 1, "file" + i + ".txt"));
        }

        // Мокаем репозиторий
        when(paymentEntityRepository.saveAll(payments)).thenReturn(payments);

        // Вызов метода сохранения большого количества платежей
        List<Payment> savedPayments = paymentService.saveAll(payments);

        // Проверка, что все платежи сохранены
        assertEquals(1000, savedPayments.size());
    }


}
