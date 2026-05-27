package com.baseball.ticket.domain.seat.scheduler;

import com.baseball.ticket.domain.seat.entity.SeatLock;
import com.baseball.ticket.domain.seat.repository.SeatLockRepository;
import com.baseball.ticket.domain.seat.dto.SeatStatusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatLockExpiredScheduler {

    private final SeatLockRepository seatLockRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    // 10초마다 만료된 좌석 선점 확인
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void releaseExpiredSeatLocks() {

        // DB에서 만료된 SeatLock 조회
        List<SeatLock> expiredLocks = seatLockRepository
                .findExpiredLocks(LocalDateTime.now());

        if (expiredLocks.isEmpty()) return;

        for (SeatLock lock : expiredLocks) {
            String gameId  = lock.getGame().getId();
            String seatId  = lock.getSeat().getId();
            String redisKey = "seat_lock:" + gameId + ":" + seatId;

            // Redis 키 삭제
            redisTemplate.delete(redisKey);

            // WebSocket AVAILABLE 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/seats/" + gameId,
                    SeatStatusMessage.builder()
                            .seatId(seatId)
                            .status("AVAILABLE")
                            .gameId(gameId)
                            .build()
            );

            log.info("좌석 선점 만료 해제 - gameId: {}, seatId: {}",
                    gameId, seatId);
        }

        // DB에서 만료된 SeatLock 삭제
        seatLockRepository.deleteAll(expiredLocks);
    }
}