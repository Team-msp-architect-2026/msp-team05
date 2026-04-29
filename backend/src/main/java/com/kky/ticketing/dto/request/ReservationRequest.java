package com.kky.ticketing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReservationRequest {

    @NotBlank
    private String lockToken;

    @NotNull
    private Long paymentId;

    @NotBlank
    private String ticketType;
}
