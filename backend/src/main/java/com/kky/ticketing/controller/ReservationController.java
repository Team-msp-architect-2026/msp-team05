package com.kky.ticketing.controller;

import com.kky.ticketing.dto.request.ReservationRequest;
import com.kky.ticketing.dto.response.ApiResponse;
import com.kky.ticketing.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReservationRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.create(userId, req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyReservations(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.getMyReservations(userId)));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.getDetail(userId, reservationId)));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long reservationId) {
        reservationService.cancel(userId, reservationId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
