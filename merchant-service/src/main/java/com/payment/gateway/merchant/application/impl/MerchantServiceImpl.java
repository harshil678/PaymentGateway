package com.payment.gateway.merchant.application.impl;

import com.payment.gateway.merchant.application.service.MerchantService;
import com.payment.gateway.merchant.domain.entity.Merchant;
import com.payment.gateway.merchant.domain.entity.MerchantApiKey;
import com.payment.gateway.merchant.domain.entity.MerchantEvent;
import com.payment.gateway.merchant.domain.enums.MerchantStatus;
import com.payment.gateway.merchant.domain.repository.MerchantApiKeyRepository;
import com.payment.gateway.merchant.domain.repository.MerchantEventRepository;
import com.payment.gateway.merchant.domain.repository.MerchantRepository;
import com.payment.gateway.merchant.dto.request.RegisterMerchantRequest;
import com.payment.gateway.merchant.dto.request.UpdateMerchantRequest;
import com.payment.gateway.merchant.dto.response.ApiKeyResponse;
import com.payment.gateway.merchant.dto.response.MerchantResponse;
import com.payment.gateway.merchant.exception.ApiLimitExceededException;
import com.payment.gateway.merchant.exception.MerchantAlreadyExistsException;
import com.payment.gateway.merchant.exception.MerchantNotFoundException;
import com.payment.gateway.merchant.infrastructure.kafka.publisher.MerchantEventPublisher;
import com.payment.gateway.merchant.mapper.ApiKeyMapper;
import com.payment.gateway.merchant.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantApiKeyRepository apiKeyRepository;
    private final MerchantEventRepository merchantEventRepository;
    private final MerchantEventPublisher eventPublisher;
    private final MerchantMapper merchantMapper;
    private final ApiKeyMapper apiKeyMapper;

    @Override
    @Transactional
    public MerchantResponse register(RegisterMerchantRequest request) {
        if (merchantRepository.existsByEmail(request.getEmail())) {
            throw new MerchantAlreadyExistsException("Merchant with email already exists: " + request.getEmail());
        }

        Merchant merchant = Merchant.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .webhookUrl(request.getWebhookUrl())
                .status(MerchantStatus.ACTIVE)
                .build();

        merchant = merchantRepository.save(merchant);
        recordEvent(merchant, "MERCHANT_REGISTERED", null);
        eventPublisher.publishMerchantCreated(merchant);

        log.info("Merchant registered: {}", merchant.getId());
        return merchantMapper.toResponse(merchant);
    }

    @Override
    public MerchantResponse getById(String merchantId) {
        Merchant merchant = findMerchantOrThrow(merchantId);
        return merchantMapper.toResponse(merchant);
    }

    @Override
    @Transactional
    public MerchantResponse update(String merchantId, UpdateMerchantRequest request) {
        Merchant merchant = findMerchantOrThrow(merchantId);

        if (request.getName() != null) merchant.setName(request.getName());
        if (request.getWebhookUrl() != null) merchant.setWebhookUrl(request.getWebhookUrl());

        merchantRepository.save(merchant);
        recordEvent(merchant, "MERCHANT_UPDATED", null);

        log.info("Merchant updated: {}", merchantId);
        return merchantMapper.toResponse(merchant);
    }

    @Override
    @Transactional
    public ApiKeyResponse generateApiKey(String merchantId) {
        Merchant merchant = findMerchantOrThrow(merchantId);

        Optional<List<MerchantApiKey>> existingKeys = apiKeyRepository.findByMerchantIdAndRevokedFalse(merchantId);
        if (existingKeys.isPresent() && existingKeys.get().size() >= 2) {
            throw new ApiLimitExceededException("Maximum API key limit reached.");
        }

        String rawSecret = generateSecureSecret();
        String prefix = "mg_key_" + UUID.randomUUID().toString().substring(0, 8);
        String keyHash = hashSecret(rawSecret);

        MerchantApiKey apiKey = MerchantApiKey.builder()
                .id(UUID.randomUUID().toString())
                .merchant(merchant)
                .keyPrefix(prefix)
                .keyHash(keyHash)
                .revoked(false)
                .build();

        apiKey = apiKeyRepository.save(apiKey);
        recordEvent(merchant, "KEY_GENERATED", "{\"keyPrefix\":\"" + prefix + "\"}");

        log.info("API key generated for merchant: {}", merchantId);

        return apiKeyMapper.toResponse(apiKey,rawSecret);

        /*return ApiKeyResponse.builder()
                .keyId(apiKey.getId())
                .keyPrefix(apiKey.getKeyPrefix())
                .rawSecret(rawSecret)
                .createdAt(apiKey.getCreatedAt().toString())
                .expiresAt(apiKey.getExpiresAt() != null ? apiKey.getExpiresAt().toString() : null)
                .build();*/
    }

    @Override
    @Transactional
    public void revokeApiKey(String merchantId, String keyId) {
        findMerchantOrThrow(merchantId);

        MerchantApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new MerchantNotFoundException("API key not found: " + keyId));

        apiKey.setRevoked(true);
        apiKeyRepository.save(apiKey);

        recordEvent(apiKey.getMerchant(), "KEY_REVOKED", "{\"keyId\":\"" + keyId + "\"}");
        log.info("API key revoked: {} for merchant: {}", keyId, merchantId);
    }

    @Override
    public boolean validateApiKey(String keyPrefix, String rawSecret) {
        return apiKeyRepository.findByKeyPrefixAndRevokedFalse(keyPrefix)
                .map(apiKey -> verifySecret(rawSecret, apiKey.getKeyHash()))
                .orElse(false);
    }

    @Override
    public List<ApiKeyResponse> getActiveKeysById(String merchantId) {
        Merchant merchant = findMerchantOrThrow(merchantId);

        return apiKeyRepository.findByMerchantIdAndRevokedFalse(merchantId)
                .orElseThrow()
                .stream()
                .map(apiKeyMapper::toResponse)
                .toList();
    }

    private Merchant findMerchantOrThrow(String merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found: " + merchantId));
    }

    private void recordEvent(Merchant merchant, String eventType, String metadata) {
        MerchantEvent event = MerchantEvent.builder()
                .id(UUID.randomUUID().toString())
                .merchant(merchant)
                .eventType(eventType)
                .metadata(metadata)
                .build();
        merchantEventRepository.save(event);
    }

    private String generateSecureSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashSecret(String rawSecret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawSecret.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private boolean verifySecret(String rawSecret, String storedHash) {
        return hashSecret(rawSecret).equals(storedHash);
    }
}
