package com.payment.gateway.notification.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class WebhookPayload {
    private String transactionId;
    private String merchantId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String processorRef;
    private String failureReason;
    private LocalDateTime timestamp;
}
