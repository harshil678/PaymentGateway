package com.payment.gateway.merchant.domain.repository;

import com.payment.gateway.merchant.domain.entity.MerchantApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantApiKeyRepository extends JpaRepository<MerchantApiKey, String> {
    List<MerchantApiKey> findByMerchantId(String merchantId);
    Optional<List<MerchantApiKey>> findByMerchantIdAndRevokedFalse(String merchantId);
    Optional<MerchantApiKey> findByKeyPrefixAndRevokedFalse(String keyPrefix);
}