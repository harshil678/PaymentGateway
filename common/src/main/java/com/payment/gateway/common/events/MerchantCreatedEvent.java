package com.payment.gateway.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCreatedEvent {
    private String eventType = "MERCHANT_CREATED";
    private String merchantId;
    private String name;
    private String email;
    private LocalDateTime createdAt;
}
