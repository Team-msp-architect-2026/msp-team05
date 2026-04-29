package com.kkybot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "ticket_open_time")
    private LocalDateTime ticketOpenTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GameStatus status;

    public enum GameStatus {
        SCHEDULED, ON_SALE, SOLD_OUT, FINISHED
    }
}
