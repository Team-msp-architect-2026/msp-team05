package com.baseball.ticket.domain.game.dto;

import com.baseball.ticket.domain.game.entity.Game;
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
                .gameId(game.getId())
                .homeTeam(TeamInfo.builder()
                        .teamId(game.getHomeTeam().getId())
                        .name(game.getHomeTeam().getName())
                        .logoUrl(game.getHomeTeam().getLogoUrl())
                        .build())
                .awayTeam(TeamInfo.builder()
                        .teamId(game.getAwayTeam().getId())
                        .name(game.getAwayTeam().getName())
                        .logoUrl(game.getAwayTeam().getLogoUrl())
                        .build())
                .stadium(StadiumInfo.builder()
                        .stadiumId(game.getStadium().getId())
                        .name(game.getStadium().getName())
                        .build())
                .startTime(game.getStartTime())
                .status(game.getStatus())
                .remainingSeats(remainingSeats)
                .build();
    }
}
