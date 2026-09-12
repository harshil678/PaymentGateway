package com.payment.gateway.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebhookDeliveryResponse {
    private String id;
    private String transactionId;
    private String merchantId;
    private String webhookUrl;
    private String status;
    private int attemptCount;
    private String lastAttemptAt;
    private String createdAt;
}
