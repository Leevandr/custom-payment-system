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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ParserService {

    private static final Logger log = LoggerFactory.getLogger(ParserService.class);
    private final PaymentService paymentService;
    private final ReporterService reporterService;

    @Value("${spring.input.directory}")
    private Path INPUT_DIRECTORY;

    public void parseFile(Path filePath) throws IOException, FileProcessingException {
        log.info("Parsing file: {}", filePath);

        List<Payment> payments = new ArrayList<>();
        boolean hasInvalidLines = false;
        boolean hasDuplicate = false;
        Set<String> paymentIds = new HashSet<>();


        if (filePath.startsWith(INPUT_DIRECTORY) && isValidFileName(filePath)) {
            try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("Parsing line: {}", line);
                    Payment payment = parseLine(line, filePath);
                    if (payment != null) {
                        log.info("Checking if payment with ID {} exists", payment.getPaymentId());

                        if (paymentIds.contains(payment.getPaymentId())) {
                            payment.setStatusCode(PaymentStatus.DUPLICATE.getCode());
                            hasDuplicate = true;
                            log.info("Payment with ID {} is a duplicate in the current file, status set to DUPLICATE", payment.getPaymentId());
                        } else {
                            paymentIds.add(payment.getPaymentId());
                            payment.setStatusCode(PaymentStatus.OK.getCode());
                        }

                        payments.add(payment);
                    } else {
                        hasInvalidLines = true;
                    }
                }
                if (!payments.isEmpty()) {
                    if (hasInvalidLines || hasDuplicate) {
                        payments.stream()
                                .filter(payment -> payment.getStatusCode() == PaymentStatus.OK.getCode())
                                .forEach(payment -> payment.setStatusCode(PaymentStatus.PARTIAL_OK.getCode()));
                    } else {
                        payments.forEach(payment -> payment.setStatusCode(PaymentStatus.FULL_SAVED.getCode()));
                    }
                    paymentService.saveAll(payments);
                    reporterService.createReport(payments, filePath.getFileName().toString());
                } else {
                    log.warn("No valid payments to save or report");
                }
            } catch (IOException e) {
                log.error("Error reading file: {}", e.getMessage());
                throw new FileProcessingException("File processing failed", e);
            }
        } else {
            log.warn("Invalid file path or name: {}", filePath);
        }
    }

    public boolean isValidFileName(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String regex = "^BCP_\\d{8}_\\d{6}_\\d{4}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        boolean isValid = matcher.matches();
        if (!isValid) {
            log.warn("File name: {} is Invalid", fileName);
        } else {
            log.warn("File name: {} is Valid!", fileName);
        }
        return isValid;
    }

    private Payment parseLine(String line, Path filePath) {


        String regex = "^\\d{12} \\d{9}-\\d{9}-\\d{9}-\\d{9}-\\d{9,10} .{0,65} + \\d{12} \\d{1,19}\\.\\d{2}$";

        line = line.replace("\uFEFF", "").trim();
        int trimmedLength = line.length();

        if (trimmedLength < 154 || trimmedLength > 156) {
            log.error("Line length is invalid: {}", trimmedLength);
            return null;
        }
        if (!line.matches(regex)) {
            log.error("Line does not match the required format: {}", line);
            return null;
        }

        try {
            log.info("Line: {}", line);
            String recordNumber = line.substring(0, 12).trim();
            String paymentId = line.substring(13, 63).trim();
            String companyName = line.substring(64, 129).trim();
            String payerInn = line.substring(130, 142).trim();
            BigDecimal amount = new BigDecimal(line.substring(143).trim());
            String fileName = filePath.getFileName().toString();

            log.info("Parsing success");
            log.info("Record number: {}", recordNumber);
            log.info("PaymentId: {}", paymentId);
            log.info("CompanyName: {}", companyName);
            log.info("PayerInn: {}", payerInn);
            log.info("Amount: {}", amount);
            log.info("File name: {}", fileName);

            return paymentService.createPayment(paymentId, recordNumber, companyName, payerInn, amount, PaymentStatus.OK.getCode(), fileName);
        } catch (IndexOutOfBoundsException e) {
            log.error("Error while extracting fields from line: {}", e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            log.error("Invalid number format in line: {}", e.getMessage());
            return null;
        }
    }

}
