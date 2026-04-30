package com.baseball.ticket.domain.seat.service;

import com.baseball.ticket.domain.auth.entity.User;
import com.baseball.ticket.domain.auth.repository.UserRepository;
import com.baseball.ticket.domain.game.entity.Game;
import com.baseball.ticket.domain.game.repository.GameRepository;
import com.baseball.ticket.domain.seat.dto.SeatLockRequest;
import com.baseball.ticket.domain.seat.dto.SeatLockResponse;
import com.baseball.ticket.domain.seat.dto.SeatMapResponse;
import com.baseball.ticket.domain.seat.dto.SeatStatusMessage;
import com.baseball.ticket.domain.seat.entity.Seat;
import com.baseball.ticket.domain.seat.entity.SeatLock;
import com.baseball.ticket.domain.seat.entity.SeatZone;
import com.baseball.ticket.domain.seat.repository.SeatLockRepository;
import com.baseball.ticket.domain.seat.repository.SeatRepository;
import com.baseball.ticket.domain.seat.repository.SeatZoneRepository;
import com.baseball.ticket.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final SeatZoneRepository seatZoneRepository;
    private final SeatLockRepository seatLockRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int LOCK_TTL_MINUTES = 5;

    // ─────────────────────────────────────────
    // 좌석 배치도 조회
    // GET /api/games/{gameId}/seats
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public SeatMapResponse getSeats(String gameId, String zoneId) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", 404));

        String stadiumId = game.getStadium().getId();

        // DB에서 좌석 목록 조회
        List<Seat> seats = seatRepository
                .findByStadiumIdAndZoneId(stadiumId, zoneId);

        // Redis에서 현재 선점 상태 반영
        // Redis key: seat_lock:{gameId}:{seatId} 가 존재하면 LOCKED
        List<SeatMapResponse.ZoneInfo> zones = buildZoneInfo(seats, gameId);

        return SeatMapResponse.builder()
                .zones(zones)
                .build();
    }

    // 좌석 목록을 구역별로 묶어서 Redis 상태 반영
    private List<SeatMapResponse.ZoneInfo> buildZoneInfo(
            List<Seat> seats, String gameId) {

        // 구역별로 그룹핑
        Map<SeatZone, List<Seat>> grouped = seats.stream()
                .collect(Collectors.groupingBy(Seat::getZone));

        List<SeatMapResponse.ZoneInfo> zones = new ArrayList<>();

        for (Map.Entry<SeatZone, List<Seat>> entry : grouped.entrySet()) {
            SeatZone zone = entry.getKey();
            List<Seat> zoneSeats = entry.getValue();

            List<SeatMapResponse.SeatInfo> seatInfos = zoneSeats.stream()
                    .map(seat -> {
                        // Redis에서 선점 여부 확인
                        String redisKey = "seat_lock:" + gameId + ":" + seat.getId();
                        boolean isLocked = Boolean.TRUE.equals(
                                redisTemplate.hasKey(redisKey));


                        String status = isLocked ? "LOCKED" : seat.getStatus();

                        return SeatMapResponse.SeatInfo.builder()
                                .seatId(seat.getId())
                                .rowNum(seat.getRowNum())
                                .number(seat.getNumber())
                                .status(status)
                                .build();
                    })
                    .collect(Collectors.toList());

            zones.add(SeatMapResponse.ZoneInfo.builder()
                    .zoneId(zone.getId())
                    .zoneName(zone.getZoneName())
                    .price(zone.getPrice())
                    .seats(seatInfos)
                    .build());
        }

        return zones;
    }

    // ─────────────────────────────────────────
    // 좌석 임시 선점 (5분 TTL)
    // POST /api/games/{gameId}/seats/lock
    // ─────────────────────────────────────────
    @Transactional
    public SeatLockResponse lockSeats(String gameId,
                                      SeatLockRequest request,
                                      String userEmail) {

        // 최대 4석 검증 (DTO @Size로도 하지만 서비스에서 한번 더)
        if (request.getSeatIds().size() > 4) {
            throw new BusinessException("EXCEED_MAX_SEATS", 400);
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", 404));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", 404));

        List<Seat> seats = seatRepository.findAllByIds(request.getSeatIds());

        if (seats.size() != request.getSeatIds().size()) {
            throw new BusinessException("NOT_FOUND", 404);
        }

        // ── Redis 분산락 ──────────────────────────
        // SETNX: 키가 없을 때만 저장 성공 → 동시에 여러 명이 눌러도 1명만 성공
        List<String> lockedKeys = new ArrayList<>();

        for (Seat seat : seats) {
            String redisKey = "seat_lock:" + gameId + ":" + seat.getId();

            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, user.getId(),
                            LOCK_TTL_MINUTES, TimeUnit.MINUTES);

            if (!Boolean.TRUE.equals(acquired)) {
                // 선점 실패 → 지금까지 건 락 모두 롤백
                rollbackRedisLocks(lockedKeys);
                throw new BusinessException("SEAT_ALREADY_LOCKED", 409);
            }

            lockedKeys.add(redisKey);

        }
        // ─────────────────────────────────────────

        // lockToken 생성 및 Redis 저장 (결제 시 검증용)
        String lockToken = UUID.randomUUID().toString();
        String seatIdsValue = String.join(",", request.getSeatIds());
        redisTemplate.opsForValue().set(
                "lock_token:" + lockToken,
                user.getId() + "|" + gameId + "|" + seatIdsValue,
                LOCK_TTL_MINUTES, TimeUnit.MINUTES
        );

        // DB에도 선점 기록 저장
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(LOCK_TTL_MINUTES);
        for (Seat seat : seats) {
            seatLockRepository.save(SeatLock.builder()
                    .seat(seat)
                    .user(user)
                    .game(game)
                    .lockToken(lockToken)
                    .expiresAt(expiresAt)
                    .build());
        }

        // 응답 빌드
        int totalAmount = seats.stream()
                .mapToInt(s -> s.getZone().getPrice())
                .sum();

        List<SeatLockResponse.SeatInfo> seatInfos = seats.stream()
                .map(s -> SeatLockResponse.SeatInfo.builder()
                        .seatId(s.getId())
                        .rowNum(s.getRowNum())
                        .number(s.getNumber())
                        .zoneName(s.getZone().getZoneName())
                        .price(s.getZone().getPrice())
                        .build())
                .collect(Collectors.toList());

        // ── WebSocket 브로드캐스트 ──────────────────
        // 선점된 좌석 상태를 같은 경기 보는 모든 사용자에게 전송
        for (Seat seat : seats) {
            messagingTemplate.convertAndSend(
                    "/topic/seats/" + gameId,   // 구독 토픽
                    SeatStatusMessage.builder()
                            .seatId(seat.getId())
                            .status("LOCKED")
                            .gameId(gameId)
                            .build()
            );
        }

        return SeatLockResponse.builder()
                .lockToken(lockToken)
                .expiresAt(expiresAt)
                .seats(seatInfos)
                .totalAmount(totalAmount)
                .build();
    }

    // Redis 락 롤백 (선점 실패 시)
    private void rollbackRedisLocks(List<String> keys) {
        // Redis 임시 비활성화 — 로컬 테스트용
        keys.forEach(redisTemplate::delete);
    }
}
