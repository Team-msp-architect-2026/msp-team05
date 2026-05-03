package com.kky.ticketing.domain.admin;

import com.kky.ticketing.domain.entity.SeatZone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatZoneRepository extends JpaRepository<SeatZone, String> {}