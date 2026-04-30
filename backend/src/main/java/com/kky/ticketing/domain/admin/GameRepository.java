package com.kky.ticketing.domain.admin;

import com.kky.ticketing.domain.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {}
