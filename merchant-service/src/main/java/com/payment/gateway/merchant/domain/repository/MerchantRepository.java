package com.payment.gateway.merchant.domain.repository;

import com.payment.gateway.merchant.domain.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String> {
    Optional<Merchant> findByEmail(String email);
    boolean existsByEmail(String email);
}