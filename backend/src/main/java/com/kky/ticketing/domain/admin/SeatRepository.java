package com.kky.ticketing.domain.admin;

import com.kky.ticketing.domain.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {}
