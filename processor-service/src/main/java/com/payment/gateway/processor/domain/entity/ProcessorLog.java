package com.payment.gateway.processor.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processor_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessorLog {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "processor_type", nullable = false)
    private String processorType;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "processor_ref")
    private String processorRef;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
