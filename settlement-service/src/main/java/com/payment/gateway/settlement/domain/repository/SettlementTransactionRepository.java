package com.payment.gateway.settlement.domain.repository;

import com.payment.gateway.settlement.domain.entity.SettlementTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementTransactionRepository extends JpaRepository<SettlementTransaction, String> {
    List<SettlementTransaction> findBySettlementId(String settlementId);
}
