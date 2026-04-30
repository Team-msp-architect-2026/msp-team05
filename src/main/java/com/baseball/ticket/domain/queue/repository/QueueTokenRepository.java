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