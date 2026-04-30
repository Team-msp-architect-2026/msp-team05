package com.baseball.ticket.domain.queue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class QueueEnterResponse {

    private String queueToken;
    private int position;
    private int estimatedWaitSeconds;
    private LocalDateTime expiresAt;
}