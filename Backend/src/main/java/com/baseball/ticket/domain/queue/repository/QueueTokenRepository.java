/*
package com.baseball.ticket.domain.queue.repository;

import com.baseball.ticket.domain.queue.entity.QueueToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QueueTokenRepository
        extends JpaRepository<QueueToken, String> {

    Optional<QueueToken> findByToken(String token);

    @Query("SELECT COUNT(q) FROM QueueToken q " +
            "WHERE q.game.id = :gameId " +
            "AND q.status = 'WAITING'")
    int countWaitingByGameId(@Param("gameId") String gameId);
}

 */



package com.baseball.ticket.domain.queue.repository;

import com.baseball.ticket.domain.queue.entity.QueueToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QueueTokenRepository
        extends JpaRepository<QueueToken, String> {

    Optional<QueueToken> findByToken(String token);

    // 내 position보다 앞에서 WAITING인 사람 수
    @Query("SELECT COUNT(q) FROM QueueToken q " +
            "WHERE q.game.id = :gameId " +
            "AND q.status = 'WAITING' " +
            "AND q.position < :myPosition")
    int countAheadInQueue(
            @Param("gameId") String gameId,
            @Param("myPosition") int myPosition);

    // 중복 진입 방지
    @Query("SELECT COUNT(q) > 0 FROM QueueToken q " +
            "WHERE q.game.id = :gameId " +
            "AND q.user.id = :userId " +
            "AND q.status IN :statuses")
    boolean existsByGameIdAndUserIdAndStatusIn(
            @Param("gameId") String gameId,
            @Param("userId") String userId,
            @Param("statuses") List<String> statuses);

    // 스케줄러용: 각 경기의 WAITING 상태 앞 N개 조회
    @Query("SELECT q FROM QueueToken q " +
            "WHERE q.game.id = :gameId " +
            "AND q.status = 'WAITING' " +
            "ORDER BY q.position ASC")
    List<QueueToken> findWaitingByGameId(@Param("gameId") String gameId);

    // 스케줄러용: 모든 경기 gameId 목록 조회
    @Query("SELECT DISTINCT q.game.id FROM QueueToken q " +
            "WHERE q.status = 'WAITING'")
    List<String> findDistinctGameIdsWithWaiting();

    // ALLOWED 상태 인원 수 조회
    @Query("SELECT COUNT(q) FROM QueueToken q " +
            "WHERE q.game.id = :gameId " +
            "AND q.status = 'ALLOWED'")
    int countAllowedByGameId(@Param("gameId") String gameId);

    @Query("SELECT q FROM QueueToken q " +
            "WHERE q.status IN ('ALLOWED', 'WAITING') " +
            "AND q.expiresAt < :now")
    List<QueueToken> findExpiredAllowedTokens(@Param("now") LocalDateTime now);

    // 만료된 WAITING/ALLOWED 토큰 조회
    @Query("SELECT q FROM QueueToken q " +
            "WHERE q.status IN ('ALLOWED', 'WAITING') " +
            "AND q.expiresAt < :now")
    List<QueueToken> findExpiredTokens(@Param("now") LocalDateTime now);
}