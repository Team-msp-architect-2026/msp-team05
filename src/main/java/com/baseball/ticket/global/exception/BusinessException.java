package com.baseball.ticket.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;

    public BusinessException(String errorCode, int statusCode) {
        super(errorCode);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
}
