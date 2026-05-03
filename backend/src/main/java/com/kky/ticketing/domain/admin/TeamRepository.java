package com.kky.ticketing.domain.admin;

import com.kky.ticketing.domain.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, String> {}