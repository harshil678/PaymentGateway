package com.payment.gateway.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    private String eventType = "PAYMENT_FAILED";
    private String transactionId;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private String failureReason;
    private LocalDateTime failedAt;
}
