package com.baseball.ticket.domain.game.repository;

import com.baseball.ticket.domain.game.entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StadiumRepository extends JpaRepository<Stadium, String> {
}