package com.kky.ticketing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PaymentPrepareRequest {

    @NotBlank
    private String lockToken;

    @NotBlank
    private String paymentMethod;
}
