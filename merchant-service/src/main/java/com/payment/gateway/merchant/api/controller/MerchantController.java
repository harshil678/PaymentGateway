package com.payment.gateway.merchant.api.controller;

import com.payment.gateway.merchant.application.service.MerchantService;
import com.payment.gateway.merchant.dto.request.RegisterMerchantRequest;
import com.payment.gateway.merchant.dto.request.UpdateMerchantRequest;
import com.payment.gateway.merchant.dto.response.ApiKeyResponse;
import com.payment.gateway.merchant.dto.response.MerchantResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping
    public ResponseEntity<MerchantResponse> register(@Valid @RequestBody RegisterMerchantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantService.register(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MerchantResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(merchantService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MerchantResponse> update(@PathVariable String id,
                                                    @RequestBody UpdateMerchantRequest request) {
        return ResponseEntity.ok(merchantService.update(id, request));
    }

    @GetMapping("/{id}/keys")
    public ResponseEntity<List<ApiKeyResponse>> getActiveKeys(@PathVariable String id)
    {
        return ResponseEntity.ok(merchantService.getActiveKeysById(id));
    }

    @PostMapping("/{id}/keys")
    public ResponseEntity<ApiKeyResponse> generateKey(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantService.generateApiKey(id));
    }

    @DeleteMapping("/{id}/keys/{keyId}")
    public ResponseEntity<Void> revokeKey(@PathVariable String id, @PathVariable String keyId) {
        merchantService.revokeApiKey(id, keyId);
        return ResponseEntity.noContent().build();
    }
}
