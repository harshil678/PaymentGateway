package com.payment.gateway.payment.domain.repository;

import com.payment.gateway.payment.domain.entity.Transaction;
import com.payment.gateway.payment.domain.enums.TransactionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Optional<Transaction> findByMerchantIdAndIdempotencyKey(String merchantId, String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Transaction t WHERE t.status = :status AND t.expiresAt < :now")
    List<Transaction> findExpiredTransactionsForUpdate(TransactionStatus status, LocalDateTime now);
    List<Transaction> findByMerchantIdOrderByCreatedAtDesc(String merchantId);
}
