package com.baseball.ticket.domain.game.dto;

import com.baseball.ticket.domain.game.entity.Game;
import com.baseball.ticket.domain.seat.entity.SeatZone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GameDetailResponse {

    private String gameId;
    private GameListResponse.TeamInfo homeTeam;
    private GameListResponse.TeamInfo awayTeam;
    private StadiumDetailInfo stadium;
    private LocalDateTime startTime;
    private LocalDateTime ticketOpenTime;
    private String status;
    private List<ZoneInfo> seatZones;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StadiumDetailInfo {
        private String stadiumId;
        private String name;
        private String mapImageUrl;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ZoneInfo {
        private String zoneId;
        private String zoneName;
        private int price;
        private int remainingSeats;
    }

    public static GameDetailResponse from(Game game, List<ZoneInfo> seatZones) {
        return GameDetailResponse.builder()
                .gameId(game.getId())
                .homeTeam(GameListResponse.TeamInfo.builder()
                        .teamId(game.getHomeTeam().getId())
                        .name(game.getHomeTeam().getName())
                        .logoUrl(game.getHomeTeam().getLogoUrl())
                        .build())
                .awayTeam(GameListResponse.TeamInfo.builder()
                        .teamId(game.getAwayTeam().getId())
                        .name(game.getAwayTeam().getName())
                        .logoUrl(game.getAwayTeam().getLogoUrl())
                        .build())
                .stadium(StadiumDetailInfo.builder()
                        .stadiumId(game.getStadium().getId())
                        .name(game.getStadium().getName())
                        .mapImageUrl(game.getStadium().getMapImageUrl())
                        .build())
                .startTime(game.getStartTime())
                .ticketOpenTime(game.getTicketOpenTime())
                .status(game.getStatus())
                .seatZones(seatZones)
                .build();
    }
}
