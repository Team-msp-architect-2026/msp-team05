package com.baseball.ticket.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PaymentConfirmRequest {

    @NotBlank
    private String orderId;

    @NotBlank
    private String pgPaymentId;
}
