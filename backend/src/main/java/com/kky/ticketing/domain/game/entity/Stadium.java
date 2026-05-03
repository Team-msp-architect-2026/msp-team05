package com.kky.ticketing.domain.game.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stadiums")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;
}
