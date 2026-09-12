package com.payment.gateway.processor.domain.repository;

import com.payment.gateway.processor.domain.entity.ProcessorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessorLogRepository extends JpaRepository<ProcessorLog, String> {
    Optional<ProcessorLog> findByTransactionId(String transactionId);
    List<ProcessorLog> findByMerchantIdOrderByCreatedAtDesc(String merchantId);
}
