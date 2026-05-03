package com.kky.ticketing.domain.game.dto;

import com.kky.ticketing.domain.game.entity.Game;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class GameListResponse {

    private String gameId;
    private TeamInfo homeTeam;
    private TeamInfo awayTeam;
    private StadiumInfo stadium;
    private LocalDateTime startTime;
    private String status;
    private int remainingSeats;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TeamInfo {
        private String teamId;
        private String name;
        private String logoUrl;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StadiumInfo {
        private String stadiumId;
        private String name;
    }

    // Entity → DTO 변환
    public static GameListResponse from(Game game, int remainingSeats) {
        return GameListResponse.builder()
                .gameId(game.getId().toString())
                .homeTeam(TeamInfo.builder()
                        .teamId(game.getHomeTeam().getId().toString())
                        .name(game.getHomeTeam().getName())
                        .logoUrl(game.getHomeTeam().getLogoUrl())
                        .build())
                .awayTeam(TeamInfo.builder()
                        .teamId(game.getAwayTeam().getId().toString())
                        .name(game.getAwayTeam().getName())
                        .logoUrl(game.getAwayTeam().getLogoUrl())
                        .build())
                .stadium(StadiumInfo.builder()
                        .stadiumId(game.getStadium().getId().toString())
                        .name(game.getStadium().getName())
                        .build())
                .startTime(game.getStartTime())
                .status(game.getStatus().name())
                .remainingSeats(remainingSeats)
                .build();
    }
}
