package com.baseball.ticket.domain.admin.dto;

import com.baseball.ticket.domain.game.entity.Game;
import com.baseball.ticket.domain.game.entity.Stadium;
import com.baseball.ticket.domain.game.entity.Team;
import com.baseball.ticket.domain.seat.entity.Seat;
import com.baseball.ticket.domain.seat.entity.SeatZone;
import lombok.Builder;
import lombok.Getter;

public class AdminResponse {

    @Getter @Builder
    public static class TeamInfo {
        private String teamId;
        private String name;
        private String logoUrl;

        public static TeamInfo from(Team team) {
            return TeamInfo.builder()
                    .teamId(team.getId())
                    .name(team.getName())
                    .logoUrl(team.getLogoUrl())
                    .build();
        }
    }

    @Getter @Builder
    public static class StadiumInfo {
        private String stadiumId;
        private String name;
        private String address;

        public static StadiumInfo from(Stadium stadium) {
            return StadiumInfo.builder()
                    .stadiumId(stadium.getId())
                    .name(stadium.getName())
                    .address(stadium.getAddress())
                    .build();
        }
    }

    @Getter @Builder
    public static class GameInfo {
        private String gameId;
        private String homeTeam;
        private String awayTeam;
        private String stadium;
        private String startTime;
        private String status;

        public static GameInfo from(Game game) {
            return GameInfo.builder()
                    .gameId(game.getId())
                    .homeTeam(game.getHomeTeam().getName())
                    .awayTeam(game.getAwayTeam().getName())
                    .stadium(game.getStadium().getName())
                    .startTime(game.getStartTime().toString())
                    .status(game.getStatus().name())
                    .build();
        }
    }

    @Getter @Builder
    public static class ZoneInfo {
        private String zoneId;
        private String zoneName;
        private Integer price;
        private Integer totalSeats;

        public static ZoneInfo from(SeatZone zone) {
            return ZoneInfo.builder()
                    .zoneId(zone.getId())
                    .zoneName(zone.getZoneName())
                    .price(zone.getPrice())
                    .totalSeats(zone.getTotalSeats())
                    .build();
        }
    }

    @Getter @Builder
    public static class SeatInfo {
        private String seatId;
        private String row;
        private String number;
        private String status;

        public static SeatInfo from(Seat seat) {
            return SeatInfo.builder()
                    .seatId(seat.getId())
                    .row(seat.getRowNum())
                    .number(seat.getNumber())
                    .status(seat.getStatus())
                    .build();
        }
    }
}