import com.levandr.custompaymentsystem.CustomPaymentSystemApplication;
import com.levandr.custompaymentsystem.entity.Payment;
import com.levandr.custompaymentsystem.exception.FileProcessingException;
import com.levandr.custompaymentsystem.service.parser.ParserService;
import com.levandr.custompaymentsystem.service.payment.PaymentService;
import com.levandr.custompaymentsystem.service.repoter.ReporterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = CustomPaymentSystemApplication.class)
class ParserServiceTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private ReporterService reporterService;

    private ParserService parserService;

    @Value("${spring.input.directory}")
    private Path inputDirectory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parserService = new ParserService(paymentService, reporterService);
        ReflectionTestUtils.setField(parserService, "inputDirectory", Path.of("/mock/directory"));
    }


    @Test
    void testParseFileWithValidLines() throws Exception {
        Path testFile = createTestFile(
                "123456789012 123456789-123456789-123456789-123456789-123456789 ACME Corp      1234567890 1000.00\n" +
                "123456789013 123456789-123456789-123456789-123456789-123456789 Tech Ltd       9876543210 2000.00"
        );

        parserService.parseFile(testFile);

        verify(paymentService, times(2)).createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyInt(), anyString());
        verify(paymentService, times(1)).saveAll(anyList());
        verify(reporterService, times(1)).createReport(anyList(), eq(testFile.getFileName().toString()));
    }

    @Test
    void testParseFileWithInvalidLines() throws Exception {
        Path testFile = createTestFile(
                "INVALID_LINE\n" +
                "123456789012 123456789-123456789-123456789-123456789-123456789 ACME Corp      1234567890 1000.00"
        );

        parserService.parseFile(testFile);

        verify(paymentService, times(1)).createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyInt(), anyString());
        verify(paymentService, times(1)).saveAll(anyList());
        verify(reporterService, times(1)).createReport(anyList(), eq(testFile.getFileName().toString()));
    }

    @Test
    void testParseFileWithEmptyFile() throws Exception {
        Path testFile = createTestFile("");

        parserService.parseFile(testFile);

        verify(paymentService, never()).createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyInt(), anyString());
        verify(paymentService, never()).saveAll(anyList());
        verify(reporterService, never()).createReport(anyList(), anyString());
    }

    @Test
    void testParseLineValid() {
        String validLine = "123456789012 123456789-123456789-123456789-123456789-123456789 ACME Corp      1234567890 1000.00";

        Payment payment = parserService.parseLine(validLine);

        assertNotNull(payment);
        assertEquals("123456789-123456789-123456789-123456789-123456789", payment.getPaymentId());
        assertEquals("ACME Corp", payment.getCompanyName());
        assertEquals("1234567890", payment.getPayerInn());
        assertEquals(BigDecimal.valueOf(1000.00), payment.getAmount());
    }

    @Test
    void testParseLineInvalid() {
        String invalidLine = "INVALID_LINE";

        Payment payment = parserService.parseLine(invalidLine);

        assertNull(payment);
    }

    @Test
    void testIsValidFileName() {
        Path validPath = Path.of("BCP_20231118_123456_0001");
        Path invalidPath = Path.of("Invalid_File_Name.txt");

        assertTrue(parserService.isValidFileName(validPath));
        assertFalse(parserService.isValidFileName(invalidPath));
    }

    @Test
    void testProcessFileWithDuplicatePayments() throws IOException, FileProcessingException {
        Path testFile = createTestFile(
                "123456789012 123456789-123456789-123456789-123456789-123456789 ACME Corp      1234567890 1000.00\n" +
                "123456789012 123456789-123456789-123456789-123456789-123456789 ACME Corp      1234567890 1000.00"
        );

        parserService.parseFile(testFile);

        verify(paymentService, times(2)).createPayment(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyInt(), anyString());
        verify(paymentService, times(1)).saveAll(anyList());
        verify(reporterService, times(1)).createReport(anyList(), eq(testFile.getFileName().toString()));
    }

    private Path createTestFile(String content) throws IOException {
        Path testFile = Files.createTempFile("testFile", ".txt");
        Files.writeString(testFile, content);
        return testFile;
    }
}
