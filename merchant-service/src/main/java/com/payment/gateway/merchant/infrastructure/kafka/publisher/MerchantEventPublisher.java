package com.payment.gateway.merchant.infrastructure.kafka.publisher;

import com.payment.gateway.merchant.domain.entity.Merchant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantEventPublisher {

    private static final String TOPIC_MERCHANT_CREATED = "merchant.created";
    private static final String TOPIC_MERCHANT_SUSPENDED = "merchant.suspended";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishMerchantCreated(Merchant merchant) {
        Map<String, String> event = Map.of(
                "eventType", "MERCHANT_CREATED",
                "merchantId", merchant.getId(),
                "email", merchant.getEmail(),
                "name", merchant.getName()
        );
        kafkaTemplate.send(TOPIC_MERCHANT_CREATED, merchant.getId(), event);
        log.info("Published MERCHANT_CREATED event for: {}", merchant.getId());
    }

    public void publishMerchantSuspended(Merchant merchant) {
        Map<String, String> event = Map.of(
                "eventType", "MERCHANT_SUSPENDED",
                "merchantId", merchant.getId()
        );
        kafkaTemplate.send(TOPIC_MERCHANT_SUSPENDED, merchant.getId(), event);
        log.info("Published MERCHANT_SUSPENDED event for: {}", merchant.getId());
    }
}
