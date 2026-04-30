package com.baseball.ticket.domain.seat.repository;

import com.baseball.ticket.domain.seat.entity.SeatLock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatLockRepository
        extends JpaRepository<SeatLock, String> {
}