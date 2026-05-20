package com.baseball.ticket.domain.game.repository;

import com.baseball.ticket.domain.game.entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StadiumRepository extends JpaRepository<Stadium, String> {
    Optional<Stadium> findByName(String name);
}