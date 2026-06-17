package com.baseball.ticket.domain.seat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class SeatLockRequest {

    @NotEmpty(message = "좌석을 선택해주세요.")
    @Size(max = 4, message = "최대 4석까지 선택 가능합니다.")
    private List<String> seatIds;

    // @NotNull(message = "CAPTCHA ID가 필요합니다.")
    private String captchaId;

    // @NotEmpty(message = "CAPTCHA 답변을 입력해주세요.")
    private String captchaAnswer;
}