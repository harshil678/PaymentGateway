package com.payment.gateway.merchant.api.controller;

import com.payment.gateway.merchant.application.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final MerchantService merchantService;

    @PostMapping("/validate-key")
    public ResponseEntity<Map<String, Boolean>> validateKey(@RequestBody Map<String, String> request) {
        String keyPrefix = request.get("keyPrefix");
        String rawSecret = request.get("rawSecret");
        boolean valid = merchantService.validateApiKey(keyPrefix, rawSecret);
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}
