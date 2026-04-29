package com.kky.ticketing.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reservationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String seatData; // JSON: {"seatIds":["A1","A2"],"gameId":"1"}

    @Column(nullable = false)
    private String ticketType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime reservedAt;

    public enum ReservationStatus {
        CONFIRMED, CANCELLED
    }

    @Builder
    public Reservation(String reservationNumber, User user, Payment payment,
                       String seatData, String ticketType) {
        this.reservationNumber = reservationNumber;
        this.user = user;
        this.payment = payment;
        this.seatData = seatData;
        this.ticketType = ticketType;
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}
