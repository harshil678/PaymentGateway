package com.payment.gateway.merchant.application.service;

import com.payment.gateway.merchant.dto.request.RegisterMerchantRequest;
import com.payment.gateway.merchant.dto.request.UpdateMerchantRequest;
import com.payment.gateway.merchant.dto.response.ApiKeyResponse;
import com.payment.gateway.merchant.dto.response.MerchantResponse;

import java.util.List;

public interface MerchantService {
    MerchantResponse register(RegisterMerchantRequest request);
    MerchantResponse getById(String merchantId);
    MerchantResponse update(String merchantId, UpdateMerchantRequest request);
    ApiKeyResponse generateApiKey(String merchantId);
    void revokeApiKey(String merchantId, String keyId);
    boolean validateApiKey(String keyPrefix, String rawSecret);

    List<ApiKeyResponse> getActiveKeysById(String id);
}
