package com.baseball.ticket.domain.queue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QueueStatusResponse {

    private int position;
    private int estimatedWaitSeconds;
    private String status;       // WAITING / ALLOWED / EXPIRED
    private boolean entryAllowed;
}