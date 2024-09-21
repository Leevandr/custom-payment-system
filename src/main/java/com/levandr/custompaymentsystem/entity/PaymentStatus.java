package com.levandr.custompaymentsystem.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    OK(1),
    PARTIAL_OK(98),
    NOT_OK(99),
    FULL_SAVED(2),
    DUPLICATE(97);

    private final int code;

    PaymentStatus(int code) {
        this.code = code;
    }

}
