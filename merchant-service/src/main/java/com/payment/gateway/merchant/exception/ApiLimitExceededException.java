package com.payment.gateway.merchant.exception;

public class ApiLimitExceededException extends RuntimeException {
    public ApiLimitExceededException(String message) {
        super(message);
    }
}
