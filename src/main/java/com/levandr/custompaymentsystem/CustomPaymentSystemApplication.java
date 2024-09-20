package com.levandr.custompaymentsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CustomPaymentSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomPaymentSystemApplication.class, args);
    }

}
