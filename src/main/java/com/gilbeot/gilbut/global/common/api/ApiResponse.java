package com.gilbeot.gilbut.global.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gilbeot.gilbut.global.common.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public static <T> ResponseEntity<ApiResponse<T>> success(
            BaseCode code,
            T data
    ) {
        ApiResponse<T> response =
                new ApiResponse<>(
                        true,
                        code.getMessage(),
                        data
                );

        return ResponseEntity
                .status(code.getStatus())
                .body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> fail(
            BaseCode code
    ) {
        ApiResponse<T> response =
                new ApiResponse<>(
                        false,
                        code.getMessage(),
                        null
                );

        return ResponseEntity
                .status(code.getStatus())
                .body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> fail(
            HttpStatus status,
            String message
    ) {
        ApiResponse<T> response =
                new ApiResponse<>(
                        false,
                        message,
                        null
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}