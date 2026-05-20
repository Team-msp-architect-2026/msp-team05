package com.baseball.ticket.domain.game.repository;

import com.baseball.ticket.domain.game.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, String> {
    Optional<Team> findByName(String name);
}