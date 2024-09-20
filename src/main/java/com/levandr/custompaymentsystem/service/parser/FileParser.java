package com.levandr.custompaymentsystem.service.parser;

import com.levandr.custompaymentsystem.entity.PaymentEntity;
import com.levandr.custompaymentsystem.entity.PaymentEntityRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileParser {

    private static final Logger log = LoggerFactory.getLogger(FileParser.class);
    private final PaymentEntityRepository paymentEntityRepository;


    @Transactional
    public void parseFile(Path filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                PaymentEntity payment = parseLine(line);
                if (isValidLine(line, filePath)) {
                    Optional<PaymentEntity> existingPayment = paymentEntityRepository.findByPaymentId(payment.getPaymentId());
                    if (existingPayment.isEmpty()) {
                        paymentEntityRepository.save(payment);
                    } else {
                        log.debug("Payment with ID {} already exists, skipping...", payment.getPaymentId());
                    }
                }
            }
        }
    }

    public boolean isValidLine(String line, Path filePath) {
        boolean lineBool = line.length() == 143;
        log.debug("In {} line\n{} isValid={}", filePath, line, lineBool);
        return lineBool;
    }

    private PaymentEntity parseLine(String line) {
        PaymentEntity payment = new PaymentEntity();
        if (line.length() < 144) {
            System.out.println("Line is too short: " + line.length());
            return null;
        } else {
            String recordNumber = line.substring(0, 12).trim();
            String paymentId = line.substring(13, 63).trim();
            String companyName = line.substring(64, 129).trim();
            String payerInn = line.substring(130, 142).trim();
            BigDecimal amount = new BigDecimal(line.substring(143).trim());

            payment.setStatus(1);

            log.debug("Payment{}", payment);

            System.out.println("Parsing success");

            return new PaymentEntity(recordNumber, paymentId, companyName, payerInn, amount);

        }
    }
}
