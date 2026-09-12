package com.payment.gateway.notification.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantServiceClient {

    @Value("${services.merchant.base-url}")
    private String merchantServiceBaseUrl;

    private final RestTemplate restTemplate;

    public Optional<String> getWebhookUrl(String merchantId) {
        try {
            Map response = restTemplate.getForObject(
                    merchantServiceBaseUrl + "/merchants/" + merchantId, Map.class);
            if (response != null && response.containsKey("webhookUrl")) {
                return Optional.ofNullable((String) response.get("webhookUrl"));
            }
        } catch (Exception e) {
            log.error("Failed to fetch webhook URL for merchant: {}", merchantId, e);
        }
        return Optional.empty();
    }
}
