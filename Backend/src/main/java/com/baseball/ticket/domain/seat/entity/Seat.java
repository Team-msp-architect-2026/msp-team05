package com.baseball.ticket.domain.seat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private SeatZone zone;

    @Column(nullable = false)
    private String rowNum;        // 예) A

    @Column(nullable = false)
    private String number;     // 예) 12

    @Column(nullable = false)
    @Builder.Default
    private String status = "AVAILABLE";
    // AVAILABLE / RESERVED / LOCKED

    public void updateStatus(String status) {
        this.status = status;
    }
}
