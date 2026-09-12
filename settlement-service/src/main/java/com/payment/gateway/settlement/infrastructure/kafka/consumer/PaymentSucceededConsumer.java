package com.payment.gateway.settlement.infrastructure.kafka.consumer;

import com.payment.gateway.common.events.PaymentSucceededEvent;
import com.payment.gateway.settlement.application.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSucceededConsumer {

    private final SettlementService settlementService;

    @KafkaListener(topics = "payment.succeeded", groupId = "settlement-service-group",
                   containerFactory = "settlementKafkaListenerContainerFactory")
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Received PAYMENT_SUCCEEDED for settlement recording: {}", event.getTransactionId());
        settlementService.recordSuccessfulPayment(event);
    }
}
