package com.baseball.ticket.domain.reservation.dto;

import com.baseball.ticket.domain.game.entity.Game;
import com.baseball.ticket.domain.reservation.entity.Reservation;
import com.baseball.ticket.domain.seat.entity.Seat;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationResponse {
    private String reservationId;
    private String homeTeam;
    private String awayTeam;
    private String stadiumName;
    private LocalDateTime startTime;
    private String zoneName;
    private String rowNum;
    private String number;
    private int price;
    private String status;
    private LocalDateTime createdAt;

    public static ReservationResponse from(Reservation r, Game game, Seat seat) {
        return ReservationResponse.builder()
                .reservationId(r.getId())
                .homeTeam(game.getHomeTeam().getName())
                .awayTeam(game.getAwayTeam().getName())
                .stadiumName(game.getStadium().getName())
                .startTime(game.getStartTime())
                .zoneName(seat.getZone().getZoneName())
                .rowNum(seat.getRowNum())
                .number(seat.getNumber())
                .price(seat.getZone().getPrice())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}