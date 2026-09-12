package com.payment.gateway.merchant.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MerchantResponse {
    private String id;
    private String name;
    private String email;
    private String webhookUrl;
    private String status;
    private String createdAt;
}