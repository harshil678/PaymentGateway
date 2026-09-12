package com.payment.gateway.common.exception;

public class PaymentGatewayException extends RuntimeException {
    private final String errorCode;

    public PaymentGatewayException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
