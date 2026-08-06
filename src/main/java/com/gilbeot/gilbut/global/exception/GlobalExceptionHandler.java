package com.gilbeot.gilbut.global.exception;

import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleCustomException(
            CustomException e
    ) {
        log.warn(
                "CustomException occurred: {}",
                e.getMessage()
        );

        return ApiResponse.fail(
                e.getErrorCode()
        );
    }

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<ApiResponse<Object>>
    handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(
                        ErrorCode.INVALID_REQUEST.getMessage()
                );

        log.warn(
                "Request validation failed: {}",
                message
        );

        return ApiResponse.fail(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<ApiResponse<Object>>
    handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        log.warn(
                "Request body parsing failed: {}",
                e.getMessage()
        );

        return ApiResponse.fail(
                ErrorCode.INVALID_REQUEST
        );
    }

    @ExceptionHandler(
            ConstraintViolationException.class
    )
    public ResponseEntity<ApiResponse<Object>>
    handleConstraintViolationException(
            ConstraintViolationException e
    ) {
        log.warn(
                "Request constraint violation: {}",
                e.getMessage()
        );

        return ApiResponse.fail(
                ErrorCode.INVALID_REQUEST
        );
    }

    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<ApiResponse<Object>>
    handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e
    ) {
        log.warn(
                "Request type mismatch: parameter={}, value={}",
                e.getName(),
                e.getValue()
        );

        return ApiResponse.fail(
                ErrorCode.INVALID_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>>
    handleException(
            Exception e
    ) {
        log.error(
                "Unexpected exception occurred",
                e
        );

        return ApiResponse.fail(
                ErrorCode.INTERNAL_SERVER_ERROR
        );
    }
}