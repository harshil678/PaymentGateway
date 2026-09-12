package com.payment.gateway.processor.infrastructure.kafka.publisher;

import com.payment.gateway.common.events.ProcessorResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessorResultPublisher {

    private static final String TOPIC_PROCESSOR_RESULT = "processor.result";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishResult(ProcessorResultEvent event) {
        kafkaTemplate.send(TOPIC_PROCESSOR_RESULT, event.getTransactionId(), event);
        log.info("Published PROCESSOR_RESULT for transaction: {} success: {}",
                event.getTransactionId(), event.isSuccess());
    }
}
