package com.baseball.ticket.domain.seat.repository;

import com.baseball.ticket.domain.seat.entity.SeatLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatLockRepository
        extends JpaRepository<SeatLock, String> {

    Optional<SeatLock> findByLockToken(String lockToken);

    List<SeatLock> findAllByLockToken(String lockToken);

    @Query("SELECT s FROM SeatLock s WHERE s.expiresAt < :now")
    List<SeatLock> findExpiredLocks(
            @Param("now") LocalDateTime now
    );
}
