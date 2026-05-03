package com.kky.ticketing.domain.seat.repository;

import com.kky.ticketing.domain.seat.entity.SeatLock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatLockRepository
        extends JpaRepository<SeatLock, String> {
}