package com.payment.gateway.payment.domain.enums;

public enum TransactionStatus {
    INITIATED,
    PROCESSING,
    SUCCESS,
    FAILED,
    TIMED_OUT,
    EXPIRED
}
