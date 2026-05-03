package com.kky.ticketing.domain.queue.service;

import com.kky.ticketing.domain.auth.entity.User;
import com.kky.ticketing.domain.auth.repository.UserRepository;
import com.kky.ticketing.domain.game.entity.Game;
import com.kky.ticketing.domain.game.repository.GameRepository;
import com.kky.ticketing.domain.queue.dto.QueueEnterRequest;
import com.kky.ticketing.domain.queue.dto.QueueEnterResponse;
import com.kky.ticketing.domain.queue.dto.QueueStatusResponse;
import com.kky.ticketing.domain.queue.entity.QueueToken;
import com.kky.ticketing.domain.queue.repository.QueueTokenRepository;
import com.kky.ticketing.global.exception.BusinessException;
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

        Game game = gameRepository.findById(Long.valueOf(request.getGameId()))
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