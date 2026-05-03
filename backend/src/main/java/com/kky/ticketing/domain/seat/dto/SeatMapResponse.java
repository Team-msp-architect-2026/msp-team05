package com.kky.ticketing.domain.seat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SeatMapResponse {

    private List<ZoneInfo> zones;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ZoneInfo {
        private String zoneId;
        private String zoneName;
        private int price;
        private List<SeatInfo> seats;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SeatInfo {
        private String seatId;
        private String rowNum;
        private String number;
        private String status;   // AVAILABLE / RESERVED / LOCKED
    }
}
