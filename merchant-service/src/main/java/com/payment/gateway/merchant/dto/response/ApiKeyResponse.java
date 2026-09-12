package com.payment.gateway.merchant.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiKeyResponse {
    private String keyId;
    private String keyPrefix;
    private String rawSecret;
    private String createdAt;
    private String expiresAt;
}