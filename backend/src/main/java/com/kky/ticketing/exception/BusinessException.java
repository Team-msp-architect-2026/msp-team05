package com.kky.ticketing.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int status;
    private final String errorCode;

    public BusinessException(int status, String errorCode) {
        super(errorCode);
        this.status = status;
        this.errorCode = errorCode;
    }
}
