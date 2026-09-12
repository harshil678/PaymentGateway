package com.payment.gateway.payment.infrastructure.kafka.publisher;

import com.payment.gateway.common.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private static final String TOPIC_PAYMENT_INITIATED  = "payment.initiated";
    private static final String TOPIC_PAYMENT_SUCCEEDED  = "payment.succeeded";
    private static final String TOPIC_PAYMENT_FAILED     = "payment.failed";
    private static final String TOPIC_PAYMENT_TIMED_OUT  = "payment.timed_out";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentInitiated(PaymentInitiatedEvent event) {
        kafkaTemplate.send(TOPIC_PAYMENT_INITIATED, event.getTransactionId(), event);
        log.info("Published PAYMENT_INITIATED: {}", event.getTransactionId());
    }

    public void publishPaymentSucceeded(PaymentSucceededEvent event) {
        kafkaTemplate.send(TOPIC_PAYMENT_SUCCEEDED, event.getTransactionId(), event);
        log.info("Published PAYMENT_SUCCEEDED: {}", event.getTransactionId());
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        kafkaTemplate.send(TOPIC_PAYMENT_FAILED, event.getTransactionId(), event);
        log.info("Published PAYMENT_FAILED: {}", event.getTransactionId());
    }

    public void publishPaymentTimedOut(PaymentTimedOutEvent event) {
        kafkaTemplate.send(TOPIC_PAYMENT_TIMED_OUT, event.getTransactionId(), event);
        log.info("Published PAYMENT_TIMED_OUT: {}", event.getTransactionId());
    }
}
