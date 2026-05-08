package com.baseball.ticket.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private int code;
    private String message;
    private T data;

    // 200 성공
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, 200, "OK", data);
    }

    // 201 생성 성공
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, 201, "CREATED", data);
    }

    // 에러
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
