package com.payment.gateway.processor.application.service;

import com.payment.gateway.common.events.PaymentInitiatedEvent;
import com.payment.gateway.common.events.ProcessorResultEvent;

public interface PaymentProcessor {
    ProcessorResultEvent process(PaymentInitiatedEvent event);
    String getProcessorType();
}
