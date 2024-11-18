package com.levandr.custompaymentsystem.service.parser;

import com.levandr.custompaymentsystem.entity.Payment;
import com.levandr.custompaymentsystem.enums.PaymentStatus;
import com.levandr.custompaymentsystem.exception.FileProcessingException;
import com.levandr.custompaymentsystem.service.payment.PaymentService;
import com.levandr.custompaymentsystem.service.repoter.ReporterService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ParserService {

    private static final Logger log = LoggerFactory.getLogger(ParserService.class);
    private final PaymentService paymentService;
    private final ReporterService reporterService;

    @Value("${spring.input.directory}")
    private Path inputDirectory;

    public void parseFile(Path filePath) throws FileProcessingException {
        log.info("Parsing file: {}", filePath);

        if (!isValidFilePath(filePath)) {
            log.warn("Invalid file path or name: {}", filePath);
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            List<Payment> payments = new ArrayList<>();
            Set<String> paymentIds = new HashSet<>();

            boolean hasInvalidLines = processFile(reader, payments, paymentIds);

            if (payments.isEmpty()) {
                log.warn("No valid payments to save or report");
                return;
            }

            updatePaymentStatuses(payments, hasInvalidLines);
            saveAndReport(payments, filePath.getFileName().toString());

        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage());
            throw new FileProcessingException("File processing failed", e);
        }
    }

    private boolean isValidFilePath(Path filePath) {
        return filePath.startsWith(inputDirectory) && isValidFileName(filePath);
    }

    public boolean isValidFileName(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String regex = "^BCP_\\d{8}_\\d{6}_\\d{4}";
        boolean isValid = Pattern.matches(regex, fileName);

        log.info("File name: {} is {}", fileName, isValid ? "valid" : "invalid");
        return isValid;
    }

    private boolean processFile(BufferedReader reader, List<Payment> payments, Set<String> paymentIds) throws IOException {
        String line;
        boolean hasInvalidLines = false;

        while ((line = reader.readLine()) != null) {
            log.info("Parsing line: {}", line);
            Payment payment = parseLine(line);

            if (payment == null) {
                hasInvalidLines = true;
                continue;
            }

            if (isDuplicate(payment, paymentIds)) {
                markAsDuplicate(payment);
            } else {
                paymentIds.add(payment.getPaymentId());
                markAsValid(payment);
            }

            payments.add(payment);
        }
        return hasInvalidLines;
    }

    private boolean isDuplicate(Payment payment, Set<String> paymentIds) {
        return paymentIds.contains(payment.getPaymentId());
    }

    private void markAsDuplicate(Payment payment) {
        payment.setStatusCode(PaymentStatus.DUPLICATE.getCode());
        log.info("Payment with ID {} is a duplicate, status set to DUPLICATE", payment.getPaymentId());
    }

    private void markAsValid(Payment payment) {
        payment.setStatusCode(PaymentStatus.OK.getCode());
    }

    public Payment parseLine(String line) {
        String cleanedLine = line.replace("\uFEFF", "").trim();

        if (!isValidLine(cleanedLine)) {
            log.error("Line is invalid: {}", cleanedLine);
            return null;
        }

        try {
            return extractPayment(cleanedLine);
        } catch (Exception e) {
            log.error("Error parsing line: {}", e.getMessage());
            return null;
        }
    }

    private boolean isValidLine(String line) {
        String regex = "^\\d{12} \\d{9}-\\d{9}-\\d{9}-\\d{9}-\\d{9,10} .{0,65} + \\d{12} \\d{1,19}\\.\\d{2}$";
        return line.length() >= 154 && line.length() <= 156 && line.matches(regex);
    }

    private Payment extractPayment(String line) {
        String recordNumber = line.substring(0, 12).trim();
        String paymentId = line.substring(13, 63).trim();
        String companyName = line.substring(64, 129).trim();
        String payerInn = line.substring(130, 142).trim();
        BigDecimal amount = new BigDecimal(line.substring(143).trim());

        log.info("Parsed payment - Record: {}, ID: {}, Company: {}, PayerInn: {}, Amount: {}",
                recordNumber, paymentId, companyName, payerInn, amount);

        return paymentService.createPayment(paymentId, recordNumber, companyName, payerInn, amount, PaymentStatus.OK.getCode(), "");
    }

    private void updatePaymentStatuses(List<Payment> payments, boolean hasInvalidLines) {
        if (hasInvalidLines) {
            payments.stream()
                    .filter(payment -> payment.getStatusCode() == PaymentStatus.OK.getCode())
                    .forEach(payment -> payment.setStatusCode(PaymentStatus.PARTIAL_OK.getCode()));
        } else {
            payments.forEach(payment -> payment.setStatusCode(PaymentStatus.FULL_SAVED.getCode()));
        }
    }

    private void saveAndReport(List<Payment> payments, String fileName) {
        paymentService.saveAll(payments);
        reporterService.createReport(payments, fileName);
    }
}

