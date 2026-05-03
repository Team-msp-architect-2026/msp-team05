package com.kky.ticketing.domain.seat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SeatLockResponse {

    private String lockToken;
    private LocalDateTime expiresAt;       // 5분 후
    private List<SeatInfo> seats;
    private int totalAmount;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SeatInfo {
        private String seatId;
        private String rowNum;
        private String number;
        private String zoneName;
        private int price;
    }
}
