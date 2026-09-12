package com.payment.gateway.merchant.domain.repository;

import com.payment.gateway.merchant.domain.entity.MerchantEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantEventRepository extends JpaRepository<MerchantEvent, String> {
    List<MerchantEvent> findByMerchantIdOrderByCreatedAtDesc(String merchantId);
}