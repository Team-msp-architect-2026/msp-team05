package com.baseball.ticket.domain.seat.controller;

import com.baseball.ticket.domain.seat.dto.SeatLockRequest;
import com.baseball.ticket.domain.seat.dto.SeatLockResponse;
import com.baseball.ticket.domain.seat.dto.SeatMapResponse;
import com.baseball.ticket.domain.seat.service.SeatService;
import com.baseball.ticket.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Seats", description = "좌석 조회 및 선점 API")
@RestController
@RequestMapping("/api/games/{gameId}/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // ─────────────────────────────────────────
    // GET /api/games/{gameId}/seats?zoneId=xxx
    // 좌석 배치도 조회
    // ─────────────────────────────────────────
    @Operation(
        summary = "좌석 배치도 조회",
        description = "경기의 구역별 좌석 목록과 실시간 상태를 반환합니다. (JWT 필요)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<SeatMapResponse>> getSeats(
            @PathVariable String gameId,
            @RequestParam(required = false) String zoneId) {

        SeatMapResponse response = seatService.getSeats(gameId, zoneId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ─────────────────────────────────────────
    // POST /api/games/{gameId}/seats/lock
    // 좌석 임시 선점 (5분 TTL) — WAF 검증 핵심 엔드포인트
    // ─────────────────────────────────────────
    @Operation(
        summary = "좌석 임시 선점 (5분 TTL)",
        description = "선택한 좌석을 5분간 임시 선점합니다. 최대 4석. Redis 분산락 적용. (JWT 필요)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/lock")
    public ResponseEntity<ApiResponse<SeatLockResponse>> lockSeats(
            @PathVariable String gameId,
            @Valid @RequestBody SeatLockRequest request,
            @AuthenticationPrincipal String email) {

        if (email == null) {
            throw new com.baseball.ticket.global.exception.BusinessException("UNAUTHORIZED", 401);
        }

        SeatLockResponse response = seatService.lockSeats(gameId, request, email);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
