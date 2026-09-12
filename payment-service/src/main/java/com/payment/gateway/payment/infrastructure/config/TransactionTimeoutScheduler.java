package com.payment.gateway.payment.infrastructure.config;

import com.payment.gateway.common.events.PaymentTimedOutEvent;
import com.payment.gateway.common.utils.IdGenerator;
import com.payment.gateway.payment.domain.entity.Transaction;
import com.payment.gateway.payment.domain.entity.TransactionEvent;
import com.payment.gateway.payment.domain.enums.TransactionStatus;
import com.payment.gateway.payment.domain.repository.TransactionEventRepository;
import com.payment.gateway.payment.domain.repository.TransactionRepository;
import com.payment.gateway.payment.infrastructure.kafka.publisher.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class TransactionTimeoutScheduler {

    private final TransactionRepository transactionRepository;
    private final TransactionEventRepository transactionEventRepository;
    private final PaymentEventPublisher eventPublisher;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void expireTimedOutTransactions() {
        List<Transaction> expiredTransactions = transactionRepository
                .findExpiredTransactionsForUpdate(TransactionStatus.PROCESSING, LocalDateTime.now());

        log.info("Found {} transactions to expire", expiredTransactions.size());
        if (expiredTransactions.isEmpty()) return;


        for (Transaction transaction : expiredTransactions) {
            TransactionStatus previous = transaction.getStatus();
            transaction.setStatus(TransactionStatus.TIMED_OUT);
            transactionRepository.save(transaction);

            TransactionEvent event = TransactionEvent.builder()
                    .id(IdGenerator.generate())
                    .transaction(transaction)
                    .fromStatus(previous.name())
                    .toStatus(TransactionStatus.TIMED_OUT.name())
                    .triggeredBy("TIMEOUT_JOB")
                    .build();
            transactionEventRepository.save(event);

            PaymentTimedOutEvent timedOutEvent = PaymentTimedOutEvent.builder()
                    .transactionId(transaction.getId())
                    .merchantId(transaction.getMerchantId())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .timedOutAt(LocalDateTime.now())
                    .build();
            eventPublisher.publishPaymentTimedOut(timedOutEvent);

            log.info("Transaction timed out: {}", transaction.getId());
        }
    }
}
