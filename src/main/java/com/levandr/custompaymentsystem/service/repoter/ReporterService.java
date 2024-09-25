package com.levandr.custompaymentsystem.service.repoter;

import com.levandr.custompaymentsystem.entity.Payment;
import com.levandr.custompaymentsystem.enums.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ReporterService {

    private static final Logger log = LoggerFactory.getLogger(ReporterService.class);
    @Value("${spring.output.error}")
    private Path reportErrorDir;
    @Value("${spring.output.success}")
    private Path reportSuccessDir;


    public void createReport(List<Payment> payments, String fileName) {
        log.info("CreateReport is Starting...");

        StringBuilder reportContent = new StringBuilder();
        for (Payment payment : payments) {
            reportContent.append(payment.toString()).append(System.lineSeparator());
        }
        Path outputPath;
        if (payments.stream().allMatch(payment ->
                payment.getStatusCode().equals(PaymentStatus.FULL_SAVED.getCode()))) {
            outputPath = reportSuccessDir.resolve("Report " + fileName);
        } else {
            outputPath = reportErrorDir.resolve("Report " + fileName);
        }
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, reportContent.toString());
            log.info("Report is saved!");
        } catch (IOException e) {
            log.error("Report don't save {0}", e);
        }
    }
}
