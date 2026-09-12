package com.payment.gateway.settlement.domain.repository;

import com.payment.gateway.settlement.domain.entity.Settlement;
import com.payment.gateway.settlement.domain.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, String> {
    Optional<Settlement> findByMerchantIdAndSettlementDateAndCurrency(
            String merchantId, LocalDate date, String currency);
    List<Settlement> findByStatus(SettlementStatus status);
    List<Settlement> findByMerchantIdOrderBySettlementDateDesc(String merchantId);
}
