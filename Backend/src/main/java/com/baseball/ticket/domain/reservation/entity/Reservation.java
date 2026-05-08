package com.baseball.ticket.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String gameId;

    @Column(nullable = false)
    private String seatId;

    @Column(nullable = false)
    private String lockToken;

    @Column(nullable = false)
    @Builder.Default
    private String status = "CONFIRMED";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}