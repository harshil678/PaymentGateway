package com.payment.gateway.payment.mapper;

import com.payment.gateway.payment.domain.entity.Transaction;
import com.payment.gateway.payment.domain.entity.TransactionEvent;
import com.payment.gateway.payment.dto.response.PaymentResponse;
import com.payment.gateway.payment.dto.response.TransactionEventResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Transaction transaction) {
        return PaymentResponse.builder()
                .transactionId(transaction.getId())
                .merchantId(transaction.getMerchantId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus().name())
                .processorRef(transaction.getProcessorRef())
                .failureReason(transaction.getFailureReason())
                .createdAt(transaction.getCreatedAt() != null ? transaction.getCreatedAt().toString() : null)
                .updatedAt(transaction.getUpdatedAt() != null ? transaction.getUpdatedAt().toString() : null)
                .build();
    }

    public TransactionEventResponse toEventResponse(TransactionEvent event) {
        return TransactionEventResponse.builder()
                .id(event.getId())
                .fromStatus(event.getFromStatus())
                .toStatus(event.getToStatus())
                .triggeredBy(event.getTriggeredBy())
                .createdAt(event.getCreatedAt() != null ? event.getCreatedAt().toString() : null)
                .build();
    }
}
