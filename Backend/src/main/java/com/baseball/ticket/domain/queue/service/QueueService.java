/*
package com.baseball.ticket.domain.queue.service;

import com.baseball.ticket.domain.auth.entity.User;
import com.baseball.ticket.domain.auth.repository.UserRepository;
import com.baseball.ticket.domain.game.entity.Game;
import com.baseball.ticket.domain.game.repository.GameRepository;
import com.baseball.ticket.domain.queue.dto.QueueEnterRequest;
import com.baseball.ticket.domain.queue.dto.QueueEnterResponse;
import com.baseball.ticket.domain.queue.dto.QueueStatusResponse;
import com.baseball.ticket.domain.queue.entity.QueueToken;
import com.baseball.ticket.domain.queue.repository.QueueTokenRepository;
import com.baseball.ticket.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueTokenRepository queueTokenRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    // 대기열 진입 — 순번 토큰 발급
    @Transactional
    public QueueEnterResponse enter(QueueEnterRequest request,
                                    String userEmail) {

        System.out.println(">>> gameId: " + request.getGameId());
        System.out.println(">>> userEmail: " + userEmail);

        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() ->
                        new BusinessException("NOT_FOUND", 404));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new BusinessException("NOT_FOUND", 404));

        // 현재 대기 인원 조회
        int waitingCount = queueTokenRepository
                .countWaitingByGameId(request.getGameId());

        // 순번 계산 (현재 대기 인원 + 1)
        int position = waitingCount + 1;

        // 대기 시간 계산 (1명당 2초 가정)
        int estimatedWaitSeconds = position * 2;

        // 테스트용 — 대기 인원 10명 미만이면 바로 ALLOWED
        String status = position <= 10 ? "ALLOWED" : "WAITING";

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        queueTokenRepository.save(QueueToken.builder()
                .user(user)
                .game(game)
                .token(token)
                .position(position)
                .status(status)
                .expiresAt(expiresAt)
                .build());

        return QueueEnterResponse.builder()
                .queueToken(token)
                .position(position)
                .estimatedWaitSeconds(estimatedWaitSeconds)
                .expiresAt(expiresAt)
                .build();
    }

    // 현재 순번 조회
    @Transactional(readOnly = true)
    public QueueStatusResponse getStatus(String token) {

        QueueToken queueToken = queueTokenRepository
                .findByToken(token)
                .orElseThrow(() ->
                        new BusinessException("NOT_FOUND", 404));

        boolean entryAllowed =
                "ALLOWED".equals(queueToken.getStatus());

        return QueueStatusResponse.builder()
                .position(queueToken.getPosition())
                .estimatedWaitSeconds(queueToken.getPosition() * 2)
                .status(queueToken.getStatus())
                .entryAllowed(entryAllowed)
                .build();
    }

    // 대기열 이탈
    @Transactional
    public void exit(String token) {
        QueueToken queueToken = queueTokenRepository
                .findByToken(token)
                .orElseThrow(() ->
                        new BusinessException("NOT_FOUND", 404));

        queueTokenRepository.delete(queueToken);
    }
}

 */



package com.baseball.ticket.domain.queue.service;

import com.baseball.ticket.domain.auth.entity.User;
import com.baseball.ticket.domain.auth.repository.UserRepository;
import com.baseball.ticket.domain.game.entity.Game;
import com.baseball.ticket.domain.game.repository.GameRepository;
import com.baseball.ticket.domain.queue.dto.QueueEnterRequest;
import com.baseball.ticket.domain.queue.dto.QueueEnterResponse;
import com.baseball.ticket.domain.queue.dto.QueueStatusResponse;
import com.baseball.ticket.domain.queue.entity.QueueToken;
import com.baseball.ticket.domain.queue.repository.QueueTokenRepository;
import com.baseball.ticket.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueTokenRepository queueTokenRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public QueueEnterResponse enter(QueueEnterRequest request,
                                    String userEmail) {

        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new BusinessException("NOT_FOUND", 404));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", 404));

        // 중복 진입 방지
        boolean alreadyInQueue = queueTokenRepository
                .existsByGameIdAndUserIdAndStatusIn(
                        request.getGameId(),
                        user.getId(),
                        java.util.List.of("WAITING", "ALLOWED")
                );
        if (alreadyInQueue) {
            throw new BusinessException("ALREADY_IN_QUEUE", 409);
        }

        // Redis INCR으로 원자적 순번 발급
        String positionKey = "queue:position:" + request.getGameId();
        Long position = redisTemplate.opsForValue().increment(positionKey);

        // 처음 생성 시 만료 시간 설정
        if (position != null && position == 1) {
            redisTemplate.expire(positionKey, 24, TimeUnit.HOURS);
        }

        int pos = position != null ? position.intValue() : 1;
        int estimatedWaitSeconds = pos * 5;

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        queueTokenRepository.save(QueueToken.builder()
                .user(user)
                .game(game)
                .token(token)
                .position(pos)
                .status("WAITING")
                .expiresAt(expiresAt)
                .build());

        log.info("대기열 진입 - gameId: {}, userEmail: {}, position: {}",
                request.getGameId(), userEmail, pos);

        return QueueEnterResponse.builder()
                .queueToken(token)
                .position(pos)
                .estimatedWaitSeconds(estimatedWaitSeconds)
                .expiresAt(expiresAt)
                .build();
    }

    @Transactional(readOnly = true)
    public QueueStatusResponse getStatus(String token) {

        QueueToken queueToken = queueTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", 404));

        // 만료 확인
        if (LocalDateTime.now().isAfter(queueToken.getExpiresAt())) {
            return QueueStatusResponse.builder()
                    .position(0)
                    .estimatedWaitSeconds(0)
                    .status("EXPIRED")
                    .entryAllowed(false)
                    .build();
        }

        boolean entryAllowed = "ALLOWED".equals(queueToken.getStatus());

        int currentPosition;
        if (entryAllowed) {
            currentPosition = 0;
        } else {
            currentPosition = queueTokenRepository
                    .countAheadInQueue(
                            queueToken.getGame().getId(),
                            queueToken.getPosition()
                    ) + 1;
        }

        int estimatedWaitSeconds = currentPosition * 5;

        return QueueStatusResponse.builder()
                .position(currentPosition)
                .estimatedWaitSeconds(estimatedWaitSeconds)
                .status(queueToken.getStatus())
                .entryAllowed(entryAllowed)
                .build();
    }

    @Transactional
    public void exit(String token) {
        QueueToken queueToken = queueTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", 404));
        queueTokenRepository.delete(queueToken);
    }
}