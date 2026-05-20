package com.baseball.ticket.domain.queue.scheduler;

import com.baseball.ticket.domain.queue.entity.QueueToken;
import com.baseball.ticket.domain.queue.repository.QueueTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final QueueTokenRepository queueTokenRepository;

    private static final int MAX_ALLOWED_PER_GAME = 5;

    // 5초마다 WAITING → ALLOWED 전환
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processQueue() {
        List<String> gameIds = queueTokenRepository
                .findDistinctGameIdsWithWaiting();

        for (String gameId : gameIds) {
            int allowedCount = queueTokenRepository
                    .countAllowedByGameId(gameId);

            int slots = MAX_ALLOWED_PER_GAME - allowedCount;
            if (slots <= 0) continue;

            List<QueueToken> waiting = queueTokenRepository
                    .findWaitingByGameId(gameId);

            int processed = 0;
            for (QueueToken token : waiting) {
                if (processed >= slots) break;

                if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
                    queueTokenRepository.delete(token);
                    continue;
                }

                token.updateStatus("ALLOWED");
                processed++;
                log.info("대기열 ALLOWED 전환 - gameId: {}, position: {}",
                        gameId, token.getPosition());
            }
        }
    }

    // 10초마다 만료된 토큰 자동 삭제
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void cleanExpiredTokens() {
        List<QueueToken> expired = queueTokenRepository
                .findExpiredTokens(LocalDateTime.now());
        if (!expired.isEmpty()) {
            queueTokenRepository.deleteAll(expired);
            log.info("만료된 대기열 토큰 삭제: {}개", expired.size());
        }
    }
}