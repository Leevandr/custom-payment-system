package com.levandr.custompaymentsystem.service.parser;

import com.levandr.custompaymentsystem.entity.Payment;
import com.levandr.custompaymentsystem.enums.PaymentStatus;
import com.levandr.custompaymentsystem.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FileParser {

    private static final Logger log = LoggerFactory.getLogger(FileParser.class);
    private final PaymentService paymentService;

    @Value("${spring.input.directory}")
    private Path INPUT_DIRECTORY;

    @Transactional
    public void parseFile(Path filePath) throws IOException {
        log.info("parseFile start... {}", filePath);
        if (filePath.startsWith(INPUT_DIRECTORY) && isValidFileName(filePath)) {
            try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("ParseFile line... {}", line);
                    var payment = parseLine(line, filePath);
                    if (payment != null) {
                        Optional<Payment> existingPayment = paymentService.findPaymentByPaymentId(payment.getPaymentId());
                        if (existingPayment.isEmpty()) {
                            payment.setStatusCode(PaymentStatus.OK.getCode());
                            paymentService.savePayment(payment);
                        } else {
                            log.info("Payment with ID {} already exists, skipping...", payment);
                            payment.setStatusCode(PaymentStatus.DUPLICATE.getCode());
                            paymentService.savePayment(payment);
                        }
                    }
                }
            }
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
        if (line.length() < 144) {
            log.error("Line is too short: {} ", line.length());
            return null;
        } else {
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

            return paymentService.createPayment(recordNumber, paymentId, companyName, payerInn, amount, PaymentStatus.OK.getCode(), fileName);
        }
    }
}
