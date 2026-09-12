package com.payment.gateway.notification.application.service;

import com.payment.gateway.notification.dto.request.WebhookPayload;

public interface WebhookService {
    void deliver(String merchantId, String webhookUrl, WebhookPayload payload);
    void retryPendingDeliveries();
}
