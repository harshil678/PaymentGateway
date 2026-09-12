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
public class ProcessorResultEvent {
    private String eventType = "PROCESSOR_RESULT";
    private String transactionId;
    private String merchantId;
    private boolean success;
    private String processorRef;
    private String failureReason;
    private LocalDateTime processedAt;
}
