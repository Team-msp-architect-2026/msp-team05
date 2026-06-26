package com.baseball.ticket.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReservationRequest {

    @NotBlank
    private String lockToken;

    @NotBlank
    private String paymentId;

    @NotBlank
    private String ticketType;
}
