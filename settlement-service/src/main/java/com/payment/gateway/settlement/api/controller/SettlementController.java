package com.payment.gateway.settlement.api.controller;

import com.payment.gateway.settlement.domain.entity.Settlement;
import com.payment.gateway.settlement.domain.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementRepository settlementRepository;

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<Settlement>> getByMerchant(@PathVariable String merchantId) {
        return ResponseEntity.ok(
                settlementRepository.findByMerchantIdOrderBySettlementDateDesc(merchantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Settlement> getById(@PathVariable String id) {
        return settlementRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
