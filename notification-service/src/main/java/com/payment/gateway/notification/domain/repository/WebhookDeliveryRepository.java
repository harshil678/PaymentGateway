package com.payment.gateway.notification.domain.repository;

import com.payment.gateway.notification.domain.entity.WebhookDelivery;
import com.payment.gateway.notification.domain.enums.WebhookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, String> {
    Optional<WebhookDelivery> findByTransactionId(String transactionId);
    List<WebhookDelivery> findByStatusAndNextRetryAtBefore(WebhookStatus status, LocalDateTime now);
    List<WebhookDelivery> findByMerchantIdOrderByCreatedAtDesc(String merchantId);
}
