package com.kky.ticketing.domain.seat.repository;

import com.kky.ticketing.domain.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("SELECT s FROM Seat s JOIN FETCH s.zone z WHERE z.stadium.id = :stadiumId AND (:zoneId IS NULL OR z.id = :zoneId)")
    List<Seat> findByStadiumIdAndZoneId(
            @Param("stadiumId") Long stadiumId,
            @Param("zoneId") Long zoneId);

    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds")
    List<Seat> findAllByIds(@Param("seatIds") List<Long> seatIds);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.zone.stadium.id = :stadiumId AND s.status = 'AVAILABLE'")
    int countAvailableByStadiumId(@Param("stadiumId") Long stadiumId);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.zone.id = :zoneId AND s.status = 'AVAILABLE'")
    int countAvailableByZoneId(@Param("zoneId") Long zoneId);
}
