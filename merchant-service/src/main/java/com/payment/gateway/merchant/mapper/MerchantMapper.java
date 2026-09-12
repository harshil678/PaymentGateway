package com.payment.gateway.merchant.mapper;

import com.payment.gateway.merchant.domain.entity.Merchant;
import com.payment.gateway.merchant.dto.response.MerchantResponse;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper {

    public MerchantResponse toResponse(Merchant merchant) {
        return MerchantResponse.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getEmail())
                .webhookUrl(merchant.getWebhookUrl())
                .status(merchant.getStatus().name())
                .createdAt(merchant.getCreatedAt() != null ? merchant.getCreatedAt().toString() : null)
                .build();
    }
}

