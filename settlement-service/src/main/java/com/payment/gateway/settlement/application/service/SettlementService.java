package com.payment.gateway.settlement.application.service;

import com.payment.gateway.common.events.PaymentSucceededEvent;

public interface SettlementService {
    void recordSuccessfulPayment(PaymentSucceededEvent event);
    void exportPendingSettlements();
}
