package com.payment.gateway.payment.api.controller;

import com.payment.gateway.payment.application.service.PaymentService;
import com.payment.gateway.payment.dto.request.PaymentRequest;
import com.payment.gateway.payment.dto.response.PaymentResponse;
import com.payment.gateway.payment.dto.response.TransactionEventResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiatePayment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getTransaction(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getTransaction(id));
    }

    @GetMapping("/{id}/getAll")
    public ResponseEntity<List<PaymentResponse>> getAllTransactions(@PathVariable String id)
    {
        return ResponseEntity.ok(paymentService.getAllTransactions(id));
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<List<TransactionEventResponse>> getTransactionEvents(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getTransactionEvents(id));
    }
}
