package com.baseball.ticket.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PaymentPrepareRequest {

    @NotBlank
    private String lockToken;

    @NotBlank
    private String paymentMethod;
}
