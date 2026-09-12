package com.payment.gateway.processor.application.impl;

import com.payment.gateway.common.events.PaymentInitiatedEvent;
import com.payment.gateway.common.events.ProcessorResultEvent;
import com.payment.gateway.processor.application.service.PaymentProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Component
public class StubBankProcessor implements PaymentProcessor {

    @Value("${processor.stub.success-rate:0.8}")
    private double successRate;

    @Value("${processor.stub.min-latency-ms:200}")
    private long minLatencyMs;

    @Value("${processor.stub.max-latency-ms:2000}")
    private long maxLatencyMs;

    private final Random random = new Random();

    @Override
    public ProcessorResultEvent process(PaymentInitiatedEvent event) {
        long latency = minLatencyMs + (long)(random.nextDouble() * (maxLatencyMs - minLatencyMs));

        log.info("StubBankProcessor processing transaction: {} with simulated latency: {}ms",
                event.getTransactionId(), latency);

        try {
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean success = random.nextDouble() < successRate;

        if (success) {
            return ProcessorResultEvent.builder()
                    .transactionId(event.getTransactionId())
                    .merchantId(event.getMerchantId())
                    .success(true)
                    .processorRef("STUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .processedAt(LocalDateTime.now())
                    .build();
        } else {
            String[] failureReasons = {
                    "INSUFFICIENT_FUNDS",
                    "CARD_DECLINED",
                    "INVALID_ACCOUNT",
                    "BANK_TIMEOUT"
            };
            String reason = failureReasons[random.nextInt(failureReasons.length)];

            return ProcessorResultEvent.builder()
                    .transactionId(event.getTransactionId())
                    .merchantId(event.getMerchantId())
                    .success(false)
                    .failureReason(reason)
                    .processedAt(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public String getProcessorType() {
        return "STUB_BANK";
    }
}
