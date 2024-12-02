package com.levandr.custompaymentsystem.service.parser;

import com.levandr.custompaymentsystem.entity.Payment;
import com.levandr.custompaymentsystem.enums.PaymentStatus;
import com.levandr.custompaymentsystem.exception.FileProcessingException;
import com.levandr.custompaymentsystem.service.payment.PaymentService;
import com.levandr.custompaymentsystem.service.repoter.ReporterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParserServiceTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private ReporterService reporterService;

    @InjectMocks
    private ParserService parserService;

    @Mock
    private BufferedReader bufferedReader;

    private static final String VALID_FILE_NAME = "BCP_20230101_120000_0001";
    private static final String INVALID_FILE_NAME = "INVALID_FILE_NAME";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testParseFile_ValidFile() throws IOException, FileProcessingException {
        // Подготовка
        Path filePath = mock(Path.class);
        when(filePath.getFileName()).thenReturn(Path.of(VALID_FILE_NAME));
        when(filePath.startsWith(any(Path.class))).thenReturn(true);

        // Имитация строки данных
        String validLine = "123456789012 1234567890123456789012345678901234567890123456789012345678901234567890123456 12345678901234567890123456789012 12.34";
        when(bufferedReader.readLine()).thenReturn(validLine, null); // Ожидаем одну строку

        // Мокируем создание платежа
        Payment payment = mock(Payment.class);
        when(paymentService.createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyInt(), anyString()))
                .thenReturn(payment);

        // Действие
        parserService.parseFile(filePath);

        // Проверка, что метод createPayment был вызван один раз
        verify(paymentService, times(1)).createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyInt(), anyString());
        verify(reporterService, times(1)).createReport(anyList(), anyString());
    }


    @Test
    void testParseFile_InvalidFileName() throws IOException, FileProcessingException {
        // Подготовка
        Path filePath = mock(Path.class);
        when(filePath.getFileName()).thenReturn(Path.of(INVALID_FILE_NAME));
        when(filePath.startsWith(any(Path.class))).thenReturn(true);

        // Действие
        parserService.parseFile(filePath);

        // Проверка
        verify(paymentService, never()).createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyInt(), anyString());
        verify(reporterService, never()).createReport(anyList(), anyString());
    }

    @Test
    void testParseFile_InvalidLine() throws IOException, FileProcessingException {
        // Подготовка
        Path filePath = mock(Path.class);
        when(filePath.getFileName()).thenReturn(Path.of(VALID_FILE_NAME));
        when(filePath.startsWith(any(Path.class))).thenReturn(true);

        String invalidLine = "Invalid Line Data";
        when(bufferedReader.readLine()).thenReturn(invalidLine, (String) null); // Тестируем одну строку

        // Действие
        parserService.parseFile(filePath);

        // Проверка
        verify(paymentService, never()).createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyInt(), anyString());
    }

    @Test
    void testParseFile_EmptyFile() throws IOException, FileProcessingException {
        // Подготовка
        Path filePath = mock(Path.class);
        when(filePath.getFileName()).thenReturn(Path.of("validFile.txt"));
        when(filePath.startsWith(any(Path.class))).thenReturn(true);

        // Возвращаем null для имитации пустого файла
        when(bufferedReader.readLine()).thenReturn(null);

        // Действие
        parserService.parseFile(filePath);

        // Проверка, что метод createPayment не был вызван (для пустого файла)
        verify(paymentService, never()).createPayment(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(BigDecimal.class),
                anyInt(),
                anyString()
        );
    }

    @Test
    void testParseFile_WithDuplicate() throws IOException, FileProcessingException {
        // Подготовка
        Path filePath = mock(Path.class);
        when(filePath.getFileName()).thenReturn(Path.of(VALID_FILE_NAME));
        when(filePath.startsWith(any(Path.class))).thenReturn(true);

        // Имитация двух одинаковых строк
        String line1 = "123456789012 1234567890123456789012345678901234567890123456789012345678901234567890123456 12345678901234567890123456789012 12.34";
        String line2 = "123456789012 1234567890123456789012345678901234567890123456789012345678901234567890123456 12345678901234567890123456789012 12.34"; // Дублированная строка
        when(bufferedReader.readLine()).thenReturn(line1, line2, null); // Ожидаем две одинаковые строки

        // Мокируем создание платежа
        Payment payment = mock(Payment.class);
        when(paymentService.createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyInt(), anyString()))
                .thenReturn(payment);

        // Действие
        parserService.parseFile(filePath);

        // Проверка, что метод createPayment был вызван один раз для дублированных данных
        verify(paymentService, times(1)).createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyInt(), anyString());
        verify(paymentService, times(1)).saveAll(anyList()); // Проверяем сохранение всех платежей
    }


    @Test
    void testProcessFile_ValidLine() throws IOException {
        // Валидная строка
        String validLine = "123456789012 1234567890123456789012345678901234567890123456789012345678901234567890123456 12345678901234567890123456789012 12.34";

        // Ожидаем, что метод parseLine вернет объект Payment
        Payment payment = parserService.parseLine(validLine);

        // Проверяем, что объект Payment не null и что статус равен "OK"
        assertNotNull(payment, "Payment should not be null");
        assertEquals(PaymentStatus.OK.getCode(), payment.getStatusCode(), "Payment status should be OK");
    }


    @Test
    void testProcessFile_InvalidLine() throws IOException {
        String invalidLine = "Invalid Line Data";

        Payment payment = parserService.parseLine(invalidLine);

        assertNull(payment);
    }

    @Test
    void testProcessFile_EmptyLine() throws IOException {
        String emptyLine = "";

        Payment payment = parserService.parseLine(emptyLine);

        assertNull(payment);
    }

    @Test
    void testIsValidFileName_ValidFileName() {
        Path validFilePath = mock(Path.class);
        when(validFilePath.getFileName()).thenReturn(Path.of(VALID_FILE_NAME));

        boolean result = parserService.isValidFileName(validFilePath);

        assertTrue(result);
    }

    @Test
    void testIsValidFileName_InvalidFileName() {
        Path invalidFilePath = mock(Path.class);
        when(invalidFilePath.getFileName()).thenReturn(Path.of(INVALID_FILE_NAME));

        boolean result = parserService.isValidFileName(invalidFilePath);

        assertFalse(result);
    }
}
