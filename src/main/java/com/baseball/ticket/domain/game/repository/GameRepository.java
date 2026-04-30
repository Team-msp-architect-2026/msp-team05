package com.baseball.ticket.domain.game.repository;

import com.baseball.ticket.domain.game.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface GameRepository extends JpaRepository<Game, String> {

    // 날짜 / 팀 / 구장 필터 + 페이지네이션
    @Query("SELECT g FROM Game g " +
           "JOIN FETCH g.homeTeam ht " +
           "JOIN FETCH g.awayTeam at " +
           "JOIN FETCH g.stadium st " +
           "WHERE (:date IS NULL OR CAST(g.startTime AS date) = :date) " +
           "AND (:teamId IS NULL OR ht.id = :teamId OR at.id = :teamId) " +
           "AND (:stadiumId IS NULL OR st.id = :stadiumId)")
    Page<Game> findGamesWithFilter(
            @Param("date") LocalDate date,
            @Param("teamId") String teamId,
            @Param("stadiumId") String stadiumId,
            Pageable pageable);
}
