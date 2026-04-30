package com.baseball.ticket.domain.seat.repository;

import com.baseball.ticket.domain.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, String> {

    // 구역 ID로 좌석 목록 조회
    @Query("SELECT s FROM Seat s JOIN FETCH s.zone z " +
           "WHERE z.stadium.id = :stadiumId " +
           "AND (:zoneId IS NULL OR z.id = :zoneId)")
    List<Seat> findByStadiumIdAndZoneId(
            @Param("stadiumId") String stadiumId,
            @Param("zoneId") String zoneId);

    // 선점할 좌석 ID 목록 조회 (한번에)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds")
    List<Seat> findAllByIds(@Param("seatIds") List<String> seatIds);

    @Query("SELECT COUNT(s) FROM Seat s " +
            "WHERE s.zone.stadium.id = :stadiumId " +
            "AND s.status = 'AVAILABLE'")
    int countAvailableByStadiumId(@Param("stadiumId") String stadiumId);

    @Query("SELECT COUNT(s) FROM Seat s " +
            "WHERE s.zone.id = :zoneId " +
            "AND s.status = 'AVAILABLE'")
    int countAvailableByZoneId(@Param("zoneId") String zoneId);
}
