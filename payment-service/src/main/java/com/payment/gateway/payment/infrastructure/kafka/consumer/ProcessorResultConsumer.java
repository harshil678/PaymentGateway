package com.payment.gateway.payment.infrastructure.kafka.consumer;

import com.payment.gateway.common.events.PaymentFailedEvent;
import com.payment.gateway.common.events.PaymentSucceededEvent;
import com.payment.gateway.common.events.ProcessorResultEvent;
import com.payment.gateway.common.utils.IdGenerator;
import com.payment.gateway.payment.domain.entity.Transaction;
import com.payment.gateway.payment.domain.entity.TransactionEvent;
import com.payment.gateway.payment.domain.enums.TransactionStatus;
import com.payment.gateway.payment.domain.repository.TransactionEventRepository;
import com.payment.gateway.payment.domain.repository.TransactionRepository;
import com.payment.gateway.payment.infrastructure.kafka.publisher.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessorResultConsumer {

    private final TransactionRepository transactionRepository;
    private final TransactionEventRepository transactionEventRepository;
    private final PaymentEventPublisher eventPublisher;

    @KafkaListener(topics = "processor.result", groupId = "payment-service-group")
    @Transactional
    public void handleProcessorResult(ProcessorResultEvent event) {
        log.info("Received processor result for transaction: {}", event.getTransactionId());

        transactionRepository.findById(event.getTransactionId()).ifPresent(transaction -> {
            if (transaction.getStatus() != TransactionStatus.PROCESSING) {
                log.warn("Transaction {} is not in PROCESSING state, ignoring result", event.getTransactionId());
                return;
            }

            TransactionStatus previousStatus = transaction.getStatus();

            if (event.isSuccess()) {
                transaction.setStatus(TransactionStatus.SUCCESS);
                transaction.setProcessorRef(event.getProcessorRef());
                transactionRepository.save(transaction);
                recordEvent(transaction, previousStatus, TransactionStatus.SUCCESS, "PROCESSOR_CALLBACK");
                publishSucceeded(transaction, event);
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailureReason(event.getFailureReason());
                transactionRepository.save(transaction);
                recordEvent(transaction, previousStatus, TransactionStatus.FAILED, "PROCESSOR_CALLBACK");
                publishFailed(transaction, event);
            }
        });
    }

    private void recordEvent(Transaction tx, TransactionStatus from, TransactionStatus to, String triggeredBy) {
        TransactionEvent event = TransactionEvent.builder()
                .id(IdGenerator.generate())
                .transaction(tx)
                .fromStatus(from.name())
                .toStatus(to.name())
                .triggeredBy(triggeredBy)
                .build();
        transactionEventRepository.save(event);
    }

    private void publishSucceeded(Transaction tx, ProcessorResultEvent result) {
        PaymentSucceededEvent event = PaymentSucceededEvent.builder()
                .transactionId(tx.getId())
                .merchantId(tx.getMerchantId())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .processorRef(result.getProcessorRef())
                .succeededAt(LocalDateTime.now())
                .build();
        eventPublisher.publishPaymentSucceeded(event);
    }

    private void publishFailed(Transaction tx, ProcessorResultEvent result) {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .transactionId(tx.getId())
                .merchantId(tx.getMerchantId())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .failureReason(result.getFailureReason())
                .failedAt(LocalDateTime.now())
                .build();
        eventPublisher.publishPaymentFailed(event);
    }
}
