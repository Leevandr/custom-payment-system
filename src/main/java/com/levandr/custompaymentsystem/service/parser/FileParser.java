package com.levandr.custompaymentsystem.service.parser;

import com.levandr.custompaymentsystem.entity.PaymentEntity;
import com.levandr.custompaymentsystem.repository.PaymentEntityRepository;
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
    private final PaymentEntityRepository paymentEntityRepository;

    @Value("${spring.input.directory}")
    private Path INPUT_DIRECTORY;


    @Transactional
    public void parseFile(Path filePath) throws IOException {
        log.info("parseFile start... {}", filePath);
        if (filePath.startsWith(INPUT_DIRECTORY) && isValidFileName(filePath)) {
            try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {

                    log.info("parseFile line... {}", line);

                    PaymentEntity payment = parseLine(line);

                    if (payment != null && isValidLine(line)) {

                        Optional<PaymentEntity> existingPayment = paymentEntityRepository.findByPaymentId(payment.getPaymentId());
                        if (existingPayment.isEmpty()) {
                            paymentEntityRepository.save(payment);
                        } else {
                            log.info("Payment with ID {} already exists, skipping...", payment.toString());
                        }
                    }
                }
            }
        }
    }

    public boolean isValidLine(String line) {
        boolean lineBool = line.length() == 143;
        log.info("line {} isValid={}", line, lineBool);
        return lineBool;
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


    private PaymentEntity parseLine(String line) {
        PaymentEntity payment = new PaymentEntity();
        log.info("Payment: {}", payment.toString());
        if (line.length() < 144) {
            log.error("Line is too short: {} ", line.length());
            return null;
        } else {
            log.info("Payment approved: {}", payment.toString());

            String recordNumber = line.substring(0, 12).trim();
            String paymentId = line.substring(13, 63).trim();
            String companyName = line.substring(64, 129).trim();
            String payerInn = line.substring(130, 142).trim();
            BigDecimal amount = new BigDecimal(line.substring(143).trim());

            payment.setStatus(1);
            log.info("Parsing success, payment is: {}", payment.toString());
            log.info("<------------------>");
            log.info("---levandr-parser---");
            log.info("<------------------>");

            return new PaymentEntity(recordNumber, paymentId, companyName, payerInn, amount);
        }
    }
}
