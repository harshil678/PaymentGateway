package com.payment.gateway.payment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentResponse {
    private String transactionId;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String processorRef;
    private String failureReason;
    private String createdAt;
    private String updatedAt;
}
