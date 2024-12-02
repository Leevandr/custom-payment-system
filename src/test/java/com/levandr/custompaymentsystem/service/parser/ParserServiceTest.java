package com.levandr.custompaymentsystem.service.parser;

import com.levandr.custompaymentsystem.entity.Payment;
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
    void testParseFile_InvalidFileName() throws IOException, FileProcessingException {
        Path filePath = mock(Path.class);
        when(filePath.getFileName()).thenReturn(Path.of(INVALID_FILE_NAME));
        when(filePath.startsWith(any(Path.class))).thenReturn(true);

        parserService.parseFile(filePath);

        verify(paymentService, never()).createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyInt(), anyString());
        verify(reporterService, never()).createReport(anyList(), anyString());
    }

    @Test
    void testParseFile_InvalidLine() throws IOException, FileProcessingException {
        Path filePath = mock(Path.class);
        when(filePath.getFileName()).thenReturn(Path.of(VALID_FILE_NAME));
        when(filePath.startsWith(any(Path.class))).thenReturn(true);

        String invalidLine = "Invalid Line Data";
        when(bufferedReader.readLine()).thenReturn(invalidLine, (String) null);

        parserService.parseFile(filePath);

        verify(paymentService, never()).createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyInt(), anyString());
    }

    @Test
    void testParseFile_EmptyFile() throws IOException, FileProcessingException {
        Path filePath = mock(Path.class);
        when(filePath.getFileName()).thenReturn(Path.of("validFile.txt"));
        when(filePath.startsWith(any(Path.class))).thenReturn(true);

        when(bufferedReader.readLine()).thenReturn(null);

        parserService.parseFile(filePath);

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
