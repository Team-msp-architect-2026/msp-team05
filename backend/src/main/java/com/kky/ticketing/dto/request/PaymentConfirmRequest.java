package com.kky.ticketing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PaymentConfirmRequest {

    @NotBlank
    private String orderId;

    @NotBlank
    private String pgPaymentId;
}
