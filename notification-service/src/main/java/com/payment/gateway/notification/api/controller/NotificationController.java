package com.payment.gateway.notification.api.controller;

import com.payment.gateway.notification.domain.entity.WebhookDelivery;
import com.payment.gateway.notification.domain.repository.WebhookDeliveryRepository;
import com.payment.gateway.notification.dto.response.WebhookDeliveryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final WebhookDeliveryRepository webhookDeliveryRepository;

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<WebhookDeliveryResponse>> getByMerchant(@PathVariable String merchantId) {
        List<WebhookDeliveryResponse> deliveries = webhookDeliveryRepository
                .findByMerchantIdOrderByCreatedAtDesc(merchantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(deliveries);
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<WebhookDeliveryResponse> getByTransaction(@PathVariable String transactionId) {
        return webhookDeliveryRepository.findByTransactionId(transactionId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private WebhookDeliveryResponse toResponse(WebhookDelivery delivery) {
        return WebhookDeliveryResponse.builder()
                .id(delivery.getId())
                .transactionId(delivery.getTransactionId())
                .merchantId(delivery.getMerchantId())
                .webhookUrl(delivery.getWebhookUrl())
                .status(delivery.getStatus().name())
                .attemptCount(delivery.getAttemptCount())
                .lastAttemptAt(delivery.getLastAttemptAt() != null ? delivery.getLastAttemptAt().toString() : null)
                .createdAt(delivery.getCreatedAt() != null ? delivery.getCreatedAt().toString() : null)
                .build();
    }
}
