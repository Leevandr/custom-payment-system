
import com.levandr.custompaymentsystem.CustomPaymentSystemApplication;
import com.levandr.custompaymentsystem.entity.Payment;
import com.levandr.custompaymentsystem.repository.PaymentEntityRepository;
import com.levandr.custompaymentsystem.service.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CustomPaymentSystemApplication.class)
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentEntityRepository paymentRepository;

    @BeforeEach
    void setUp() {
        // Убедимся, что база данных очищена перед каждым тестом
        paymentRepository.deleteAll();
    }

    @Test
    void testSavePayment() {
        // Создание платежа
        Payment payment = new Payment();
        payment.setPaymentId("12345");
        payment.setCompanyName("ACME Corp");
        payment.setPayerInn("1234567890");
        payment.setAmount(BigDecimal.valueOf(1000.00));

        // Сохранение платежа
        Payment savedPayment = paymentService.savePayment(payment);

        // Проверки
        assertNotNull(savedPayment);
        assertNotNull(savedPayment.getId());
        assertEquals("12345", savedPayment.getPaymentId());
    }

    @Test
    void testFindDuplicatePayment() {
        // Создание и сохранение платежа
        Payment payment = new Payment();
        payment.setPaymentId("12345");
        payment.setCompanyName("ACME Corp");
        payment.setPayerInn("1234567890");
        payment.setAmount(BigDecimal.valueOf(1000.00));

        paymentService.savePayment(payment);

        // Поиск дубликата
        Optional<Payment> duplicate = paymentService.findPaymentByPaymentId("12345");

        // Проверки
        assertTrue(duplicate.isPresent());
        assertEquals("12345", duplicate.get().getPaymentId());
    }

    @Test
    void testSaveAllPayments() {
        // Создание списка платежей
        Payment payment1 = new Payment();
        payment1.setPaymentId("12345");
        payment1.setCompanyName("ACME Corp");
        payment1.setPayerInn("1234567890");
        payment1.setAmount(BigDecimal.valueOf(1000.00));

        Payment payment2 = new Payment();
        payment2.setPaymentId("67890");
        payment2.setCompanyName("Tech Ltd");
        payment2.setPayerInn("9876543210");
        payment2.setAmount(BigDecimal.valueOf(500.00));

        // Сохранение платежей
        paymentService.saveAll(List.of(payment1, payment2));

        // Проверки
        Optional<Payment> savedPayment1 = paymentService.findPaymentByPaymentId("12345");
        Optional<Payment> savedPayment2 = paymentService.findPaymentByPaymentId("67890");

        assertTrue(savedPayment1.isPresent());
        assertTrue(savedPayment2.isPresent());

        assertEquals("12345", savedPayment1.get().getPaymentId());
        assertEquals("67890", savedPayment2.get().getPaymentId());
    }

    @Test
    void testSavePaymentWithNullValues() {
        // Создание платежа с null-значениями
        Payment payment = new Payment();
        payment.setPaymentId(null);
        payment.setCompanyName(null);
        payment.setPayerInn(null);
        payment.setAmount(null);

        // Проверка, что сохранение бросает исключение
        assertThrows(Exception.class, () -> paymentService.savePayment(payment));
    }
}
