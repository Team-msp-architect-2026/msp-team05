package com.baseball.ticket.domain.seat.repository;

import com.baseball.ticket.domain.seat.entity.SeatLock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatLockRepository
        extends JpaRepository<SeatLock, String> {

    Optional<SeatLock> findByLockToken(String lockToken);

    List<SeatLock> findAllByLockToken(String lockToken);
}
