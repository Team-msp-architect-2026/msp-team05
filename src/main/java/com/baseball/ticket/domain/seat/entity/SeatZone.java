package com.baseball.ticket.domain.seat.entity;

import com.baseball.ticket.domain.game.entity.Stadium;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "seat_zones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SeatZone {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    @Column(nullable = false)
    private String zoneName;    // 예) 1루 지정석

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int totalSeats;
}
