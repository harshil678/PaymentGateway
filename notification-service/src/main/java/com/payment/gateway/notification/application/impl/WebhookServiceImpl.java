package com.payment.gateway.notification.application.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.payment.gateway.common.utils.IdGenerator;
import com.payment.gateway.notification.application.service.WebhookService;
import com.payment.gateway.notification.domain.entity.WebhookDelivery;
import com.payment.gateway.notification.domain.enums.WebhookStatus;
import com.payment.gateway.notification.domain.repository.WebhookDeliveryRepository;
import com.payment.gateway.notification.dto.request.WebhookPayload;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private static final int MAX_ATTEMPTS = 2;
    private static final long[] BACKOFF_SECONDS = {30, 60, 120, 300, 600};

    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    @Override
    @Transactional
    public void deliver(String merchantId, String webhookUrl, WebhookPayload payload) {
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);

        } catch (Exception e) {
            log.error("Failed to serialize webhook payload for merchant: {}", merchantId, e);
            return;
        }

        WebhookDelivery delivery = WebhookDelivery.builder()
                .id(IdGenerator.generate())
                .transactionId(payload.getTransactionId())
                .merchantId(merchantId)
                .webhookUrl(webhookUrl)
               .payload(payloadJson)
                .status(WebhookStatus.PENDING)
                .build();

        delivery = webhookDeliveryRepository.save(delivery);
        attemptDelivery(delivery, payload);
    }

    @Override
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void retryPendingDeliveries() {
        List<WebhookDelivery> pendingDeliveries = webhookDeliveryRepository
                .findByStatusAndNextRetryAtBefore(WebhookStatus.PENDING, LocalDateTime.now());

        if (pendingDeliveries.isEmpty()) return;

        log.info("Retrying {} pending webhook deliveries", pendingDeliveries.size());

        for (WebhookDelivery delivery : pendingDeliveries) {
            try {
                WebhookPayload payload = objectMapper.readValue(delivery.getPayload(), WebhookPayload.class);
                attemptDelivery(delivery, payload);
            } catch (Exception e) {
                log.error("Failed to deserialize payload for delivery: {}", delivery.getId(), e);
            }
        }
    }

    private void attemptDelivery(WebhookDelivery delivery, WebhookPayload payload) {
        delivery.setAttemptCount(delivery.getAttemptCount() + 1);
        delivery.setLastAttemptAt(LocalDateTime.now());

        try {
            HttpHeaders headers = new HttpHeaders();
             headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WebhookPayload> request = new HttpEntity<>(payload, headers);

            //----debug only -------------------------------------------
//            log.info("WRITE_DATES_AS_TIMESTAMPS={}",
//                    objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
//            log.info("ObjectMapper class={}",
//                    objectMapper.getClass().getName());
//            ObjectMapper mapper = new ObjectMapper();
//            //mapper.registerModule(new JavaTimeModule());
//            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//            System.out.println("Webhook payload JSON: " + mapper.writeValueAsString(payload));
//            System.out.println(
//                    objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//            );
//
//            System.out.println(objectMapper.writeValueAsString(payload));
//            System.out.println(restTemplate.getMessageConverters());
            //-----------------------------------------------------------

            ResponseEntity<String> response = restTemplate.postForEntity(
                    delivery.getWebhookUrl(), request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                delivery.setStatus(WebhookStatus.SUCCESS);
                delivery.setNextRetryAt(null);
                log.info("Webhook delivered successfully for transaction: {} attempt: {}",
                        delivery.getTransactionId(), delivery.getAttemptCount());
            } else {
                handleFailure(delivery, "Non-2xx response: " + response.getStatusCode());
            }
        } catch (Exception e) {
            handleFailure(delivery, e.getMessage());
        }

        webhookDeliveryRepository.save(delivery);
    }

    private void handleFailure(WebhookDelivery delivery, String reason) {
        log.warn("Webhook delivery failed for transaction: {} attempt: {} reason: {}",
                delivery.getTransactionId(), delivery.getAttemptCount(), reason);

        if (delivery.getAttemptCount() >= MAX_ATTEMPTS) {
            delivery.setStatus(WebhookStatus.FAILED);
            log.error("Webhook permanently failed after {} attempts for transaction: {}",
                    MAX_ATTEMPTS, delivery.getTransactionId());
        } else {
            long backoffSeconds = BACKOFF_SECONDS[delivery.getAttemptCount() - 1];
            delivery.setNextRetryAt(LocalDateTime.now().plusSeconds(backoffSeconds));
            log.info("Webhook retry scheduled in {}s for transaction: {}",
                    backoffSeconds, delivery.getTransactionId());
        }
    }
}
