package com.techeer.carpool.global.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {

    private String message;
    private T data;

    public static <T> ApiResponse<T> of(String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> of(String message) {
        return ApiResponse.<Void>builder()
                .message(message)
                .build();
    }
}
