package com.malgn.common.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int status;
    private final String message;
    private final T data;
    private final List<FieldError> errors;

    @Builder
    private ApiResponse(int status, String message, T data, List<FieldError> errors) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message("OK")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .status(201)
                .message("Created")
                .data(data)
                .build();
    }

    public static ApiResponse<Void> noContent() {
        return ApiResponse.<Void>builder()
                .status(204)
                .message("No Content")
                .build();
    }

    public static ApiResponse<Void> error(int status, String message) {
        return ApiResponse.<Void>builder()
                .status(status)
                .message(message)
                .build();
    }

    public static ApiResponse<Void> error(int status, String message, List<FieldError> errors) {
        return ApiResponse.<Void>builder()
                .status(status)
                .message(message)
                .errors(errors)
                .build();
    }

    public record FieldError(String field, String message) {
    }
}
