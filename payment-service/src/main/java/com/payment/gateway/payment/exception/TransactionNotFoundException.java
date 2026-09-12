package com.payment.gateway.payment.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) { super(message); }
}
