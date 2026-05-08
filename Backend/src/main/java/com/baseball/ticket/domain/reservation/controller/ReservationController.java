package com.baseball.ticket.domain.reservation.controller;

import com.baseball.ticket.domain.reservation.dto.ReservationRequest;
import com.baseball.ticket.domain.reservation.dto.ReservationResponse;
import com.baseball.ticket.domain.reservation.service.ReservationService;
import com.baseball.ticket.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @RequestBody ReservationRequest req,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.ok(
                reservationService.createReservation(req, email)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservations(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.ok(
                reservationService.getReservations(email)));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(
            @PathVariable String reservationId,
            @AuthenticationPrincipal String email) {
        reservationService.cancelReservation(reservationId, email);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(
            @PathVariable String reservationId,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.ok(
                reservationService.getReservation(reservationId, email)));
    }
}