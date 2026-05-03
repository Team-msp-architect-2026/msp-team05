package com.kky.ticketing.domain.seat.repository;

import com.kky.ticketing.domain.seat.entity.SeatZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatZoneRepository extends JpaRepository<SeatZone, Long> {

    // 구장 ID로 구역 목록 조회
    List<SeatZone> findByStadiumId(Long stadiumId);

    // 구역별 잔여 좌석 수 계산
    @Query("SELECT sz, COUNT(s) as remaining FROM SeatZone sz " +
           "JOIN sz.stadium st " +
           "JOIN Seat s ON s.zone.id = sz.id " +
           "WHERE st.id = :stadiumId AND s.status = 'AVAILABLE' " +
           "GROUP BY sz.id")
    List<Object[]> findZonesWithRemainingSeats(@Param("stadiumId") Long stadiumId);
}
