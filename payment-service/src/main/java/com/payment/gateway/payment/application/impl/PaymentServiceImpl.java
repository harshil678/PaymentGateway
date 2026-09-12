package com.payment.gateway.payment.application.impl;

import com.payment.gateway.common.events.PaymentInitiatedEvent;
import com.payment.gateway.common.utils.IdGenerator;
import com.payment.gateway.payment.application.service.PaymentService;
import com.payment.gateway.payment.domain.entity.Transaction;
import com.payment.gateway.payment.domain.entity.TransactionEvent;
import com.payment.gateway.payment.domain.enums.TransactionStatus;
import com.payment.gateway.payment.domain.repository.TransactionEventRepository;
import com.payment.gateway.payment.domain.repository.TransactionRepository;
import com.payment.gateway.payment.dto.request.PaymentRequest;
import com.payment.gateway.payment.dto.response.PaymentResponse;
import com.payment.gateway.payment.dto.response.TransactionEventResponse;
import com.payment.gateway.payment.exception.DuplicatePaymentException;
import com.payment.gateway.payment.exception.TransactionNotFoundException;
import com.payment.gateway.payment.infrastructure.kafka.publisher.PaymentEventPublisher;
import com.payment.gateway.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final TransactionEventRepository transactionEventRepository;
    private final PaymentEventPublisher eventPublisher;
    private final PaymentMapper paymentMapper;

    private static final int TRANSACTION_EXPIRY_MINUTES = 30;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {

        Optional<Transaction> existing = transactionRepository
                .findByMerchantIdAndIdempotencyKey(request.getMerchantId(), request.getIdempotencyKey());

        if (existing.isPresent()) {
            log.info("Duplicate payment request detected for idempotency key: {}", request.getIdempotencyKey());
            return paymentMapper.toResponse(existing.get());
        }

        Transaction transaction = Transaction.builder()
                .id(IdGenerator.generate())
                .merchantId(request.getMerchantId())
                .idempotencyKey(request.getIdempotencyKey())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(TransactionStatus.INITIATED)
                .metadata(request.getMetadata())
                .expiresAt(LocalDateTime.now().plusMinutes(TRANSACTION_EXPIRY_MINUTES))
                .build();

        try {
            transaction = transactionRepository.save(transaction);
        }
        catch (DataIntegrityViolationException e) {
            log.info("Concurrent duplicate request detected for idempotency key: {}", request.getIdempotencyKey());
            return paymentMapper.toResponse(
                    transactionRepository.findByMerchantIdAndIdempotencyKey(
                            request.getMerchantId(), request.getIdempotencyKey()
                    ).orElseThrow()
            );
        }
        recordTransitionEvent(transaction, null, TransactionStatus.INITIATED, "MERCHANT_REQUEST");

        transaction.setStatus(TransactionStatus.PROCESSING);
        transaction = transactionRepository.save(transaction);
        recordTransitionEvent(transaction, TransactionStatus.INITIATED, TransactionStatus.PROCESSING, "MERCHANT_REQUEST");

        PaymentInitiatedEvent event = PaymentInitiatedEvent.builder()
                .transactionId(transaction.getId())
                .merchantId(transaction.getMerchantId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .idempotencyKey(transaction.getIdempotencyKey())
                .initiatedAt(transaction.getCreatedAt())
                .build();

        eventPublisher.publishPaymentInitiated(event);
        log.info("Payment initiated: {}", transaction.getId());

        return paymentMapper.toResponse(transaction);
    }

    @Override
    public PaymentResponse getTransaction(String transactionId) {
        Transaction transaction = findTransactionOrThrow(transactionId);
        return paymentMapper.toResponse(transaction);
    }

    @Override
    public List<TransactionEventResponse> getTransactionEvents(String transactionId) {
        findTransactionOrThrow(transactionId);
        return transactionEventRepository
                .findByTransactionIdOrderByCreatedAtAsc(transactionId)
                .stream()
                .map(paymentMapper::toEventResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getAllTransactions(String merchantId) {
        return transactionRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }


    private Transaction findTransactionOrThrow(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));
    }

    public void recordTransitionEvent(Transaction transaction, TransactionStatus from,
                                       TransactionStatus to, String triggeredBy) {
        TransactionEvent event = TransactionEvent.builder()
                .id(IdGenerator.generate())
                .transaction(transaction)
                .fromStatus(from != null ? from.name() : "NONE")
                .toStatus(to.name())
                .triggeredBy(triggeredBy)
                .build();
        transactionEventRepository.save(event);
    }
}
