package com.baseball.ticket.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReservationRequest {
    @NotBlank
    private String lockToken;
    @NotBlank
    private String gameId;
}