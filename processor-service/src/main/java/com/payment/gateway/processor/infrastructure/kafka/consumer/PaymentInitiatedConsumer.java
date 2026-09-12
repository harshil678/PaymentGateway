package com.payment.gateway.processor.infrastructure.kafka.consumer;

import com.payment.gateway.common.events.PaymentInitiatedEvent;
import com.payment.gateway.common.events.ProcessorResultEvent;
import com.payment.gateway.common.utils.IdGenerator;
import com.payment.gateway.processor.application.service.PaymentProcessor;
import com.payment.gateway.processor.domain.entity.ProcessorLog;
import com.payment.gateway.processor.domain.repository.ProcessorLogRepository;
import com.payment.gateway.processor.infrastructure.kafka.publisher.ProcessorResultPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentInitiatedConsumer {

    private final PaymentProcessor paymentProcessor;
    private final ProcessorResultPublisher resultPublisher;
    private final ProcessorLogRepository processorLogRepository;

    @KafkaListener(topics = "payment.initiated", groupId = "processor-service-group")
    public void handlePaymentInitiated(PaymentInitiatedEvent event) {
        log.info("Received PAYMENT_INITIATED for transaction: {}", event.getTransactionId());

        if (processorLogRepository.findByTransactionId(event.getTransactionId()).isPresent()) {
            log.warn("Transaction {} already processed, skipping", event.getTransactionId());
            return;
        }

        long start = System.currentTimeMillis();
        ProcessorResultEvent result = paymentProcessor.process(event);
        long latency = System.currentTimeMillis() - start;

        ProcessorLog processorLog = ProcessorLog.builder()
                .id(IdGenerator.generate())
                .transactionId(event.getTransactionId())
                .merchantId(event.getMerchantId())
                .processorType(paymentProcessor.getProcessorType())
                .success(result.isSuccess())
                .processorRef(result.getProcessorRef())
                .failureReason(result.getFailureReason())
                .latencyMs(latency)
                .build();

        try {
            processorLogRepository.save(processorLog);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate processor log for transaction {}, skipping publish", event.getTransactionId());
            return;
        }
        resultPublisher.publishResult(result);

        log.info("Transaction {} processed in {}ms success: {}",
                event.getTransactionId(), latency, result.isSuccess());
    }
}
