package com.kky.ticketing.controller;

import com.kky.ticketing.dto.request.SeatLockRequest;
import com.kky.ticketing.dto.response.ApiResponse;
import com.kky.ticketing.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("/{gameId}/seats/lock")
    public ResponseEntity<ApiResponse<Map<String, Object>>> lockSeats(
            @PathVariable String gameId,
            @Valid @RequestBody SeatLockRequest req) {

        if (req.getSeatIds().size() > 4) {
            throw new BusinessException(400, "MAX_SEAT_EXCEEDED");
        }

        String lockToken = UUID.randomUUID().toString();
        String seatIdsJson = req.getSeatIds().stream()
                .map(id -> "\"" + id + "\"")
                .collect(Collectors.joining(",", "[", "]"));
        String lockData = "{\"seatIds\":" + seatIdsJson + ",\"gameId\":\"" + gameId + "\"}";

        // 10분간 좌석 선점
        redisTemplate.opsForValue().set("lock:" + lockToken, lockData, Duration.ofMinutes(10));

        return ResponseEntity.ok(ApiResponse.ok(Map.of("lockToken", lockToken)));
    }
}
