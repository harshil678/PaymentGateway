package com.payment.gateway.merchant.mapper;

import com.payment.gateway.merchant.domain.entity.Merchant;
import com.payment.gateway.merchant.domain.entity.MerchantApiKey;
import com.payment.gateway.merchant.dto.response.ApiKeyResponse;
import com.payment.gateway.merchant.dto.response.MerchantResponse;
import org.springframework.stereotype.Component;

import static org.apache.naming.SelectorContext.prefix;

@Component
public class ApiKeyMapper {

    public ApiKeyResponse toResponse(MerchantApiKey apiKey, String rawSecret) {
        return ApiKeyResponse.builder()
                .keyId(apiKey.getId())
                .keyPrefix(apiKey.getKeyPrefix())
                .rawSecret(rawSecret)
                .createdAt(apiKey.getCreatedAt().toString())
                .expiresAt(apiKey.getExpiresAt() != null ? apiKey.getExpiresAt().toString() : null)
                .build();
    }

    public ApiKeyResponse toResponse(MerchantApiKey apiKey) {
        return ApiKeyResponse.builder()
                .keyId(apiKey.getId())
                .keyPrefix(apiKey.getKeyPrefix())
                .rawSecret("")
                .createdAt(apiKey.getCreatedAt().toString())
                .expiresAt(apiKey.getExpiresAt() != null ? apiKey.getExpiresAt().toString() : null)
                .build();
    }
}

