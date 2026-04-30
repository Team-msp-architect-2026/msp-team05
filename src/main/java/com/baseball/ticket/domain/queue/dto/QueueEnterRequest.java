package com.baseball.ticket.domain.queue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class QueueEnterRequest {

    @NotBlank
    private String gameId;
}