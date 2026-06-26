package com.baseball.ticket.domain.auth.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class SeatLockRequest {

    @NotEmpty
    private List<String> seatIds;
}
