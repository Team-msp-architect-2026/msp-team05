package com.kky.ticketing.domain.seat.entity;

import com.kky.ticketing.domain.game.entity.Stadium;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat_zones")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    @Column(name = "zone_name", nullable = false)
    private String zoneName;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;
}
