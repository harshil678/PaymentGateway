package com.payment.gateway.merchant.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMerchantRequest {
    private String name;
    private String webhookUrl;
}