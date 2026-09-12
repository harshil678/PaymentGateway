package com.payment.gateway.notification.infrastructure.kafka.consumer;

import com.payment.gateway.common.events.PaymentFailedEvent;
import com.payment.gateway.common.events.PaymentSucceededEvent;
import com.payment.gateway.common.events.PaymentTimedOutEvent;
import com.payment.gateway.notification.application.service.WebhookService;
import com.payment.gateway.notification.dto.request.WebhookPayload;
import com.payment.gateway.notification.infrastructure.config.MerchantServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutcomeConsumer {

    private final WebhookService webhookService;
    private final MerchantServiceClient merchantServiceClient;

    @KafkaListener(topics = "payment.succeeded", groupId = "notification-service-group",
                   containerFactory = "succeededKafkaListenerContainerFactory")
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Received PAYMENT_SUCCEEDED for transaction: {}", event.getTransactionId());
        merchantServiceClient.getWebhookUrl(event.getMerchantId()).ifPresent(webhookUrl -> {
            WebhookPayload payload = WebhookPayload.builder()
                    .transactionId(event.getTransactionId())
                    .merchantId(event.getMerchantId())
                    .status("SUCCESS")
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .processorRef(event.getProcessorRef())
                    .timestamp(LocalDateTime.now())
                    .build();
            webhookService.deliver(event.getMerchantId(), webhookUrl, payload);
        });
    }

    @KafkaListener(topics = "payment.failed", groupId = "notification-service-group",
                   containerFactory = "failedKafkaListenerContainerFactory")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Received PAYMENT_FAILED for transaction: {}", event.getTransactionId());
        merchantServiceClient.getWebhookUrl(event.getMerchantId()).ifPresent(webhookUrl -> {
            WebhookPayload payload = WebhookPayload.builder()
                    .transactionId(event.getTransactionId())
                    .merchantId(event.getMerchantId())
                    .status("FAILED")
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .failureReason(event.getFailureReason())
                    .timestamp(LocalDateTime.now())
                    .build();
            webhookService.deliver(event.getMerchantId(), webhookUrl, payload);
        });
    }

    @KafkaListener(topics = "payment.timed_out", groupId = "notification-service-group",
                   containerFactory = "timedOutKafkaListenerContainerFactory")
    public void handlePaymentTimedOut(PaymentTimedOutEvent event) {
        log.info("Received PAYMENT_TIMED_OUT for transaction: {}", event.getTransactionId());
        merchantServiceClient.getWebhookUrl(event.getMerchantId()).ifPresent(webhookUrl -> {
            WebhookPayload payload = WebhookPayload.builder()
                    .transactionId(event.getTransactionId())
                    .merchantId(event.getMerchantId())
                    .status("TIMED_OUT")
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .timestamp(LocalDateTime.now())
                    .build();
            webhookService.deliver(event.getMerchantId(), webhookUrl, payload);
        });
    }
}
