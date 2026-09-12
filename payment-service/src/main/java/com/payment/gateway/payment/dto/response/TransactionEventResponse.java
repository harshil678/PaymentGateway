package com.payment.gateway.payment.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransactionEventResponse {
    private String id;
    private String fromStatus;
    private String toStatus;
    private String triggeredBy;
    private String createdAt;
}
