package com.kky.ticketing.domain.game.repository;

import com.kky.ticketing.domain.game.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {}
