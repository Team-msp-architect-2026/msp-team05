package com.kky.ticketing.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.time.LocalDateTime;

public class AdminRequest {

    @Getter
    public static class TeamCreate {
        @NotBlank
        private String name;
        private String logoUrl;
    }

    @Getter
    public static class StadiumCreate {
        @NotBlank
        private String name;
        private String address;
    }

    @Getter
    public static class GameCreate {
        @NotNull
        private Long homeTeamId;
        @NotNull
        private Long awayTeamId;
        @NotNull
        private Long stadiumId;
        @NotNull
        private LocalDateTime startTime;
        private LocalDateTime ticketOpenTime;
    }

    @Getter
    public static class ZoneCreate {
        @NotNull
        private Long stadiumId;
        @NotBlank
        private String zoneName;
        @NotNull
        private Integer price;
        @NotNull
        private Integer totalSeats;
    }

    @Getter
    public static class SeatCreate {
        @NotNull
        private Long zoneId;
        @NotBlank
        private String row;
        @NotBlank
        private String number;
    }
}
