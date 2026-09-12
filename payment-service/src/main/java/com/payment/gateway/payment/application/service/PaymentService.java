package com.payment.gateway.payment.application.service;

import com.payment.gateway.payment.dto.request.PaymentRequest;
import com.payment.gateway.payment.dto.response.PaymentResponse;
import com.payment.gateway.payment.dto.response.TransactionEventResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse initiatePayment(PaymentRequest request);
    PaymentResponse getTransaction(String transactionId);
    List<TransactionEventResponse> getTransactionEvents(String transactionId);
    List<PaymentResponse> getAllTransactions(String id);
}
