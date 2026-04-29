package com.kky.ticketing.controller;

import com.kky.ticketing.dto.request.PaymentConfirmRequest;
import com.kky.ticketing.dto.request.PaymentPrepareRequest;
import com.kky.ticketing.dto.response.ApiResponse;
import com.kky.ticketing.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<Map<String, Object>>> prepare(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PaymentPrepareRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.prepare(userId, req)));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirm(
            @Valid @RequestBody PaymentConfirmRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.confirm(req)));
    }
}
