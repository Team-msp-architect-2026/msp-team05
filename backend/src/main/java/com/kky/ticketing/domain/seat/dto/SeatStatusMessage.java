package com.kky.ticketing.domain.seat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SeatStatusMessage {

    private String seatId;
    private String status;   // AVAILABLE / LOCKED / RESERVED
    private String gameId;
}