package com.heritage.platform.exception;

import com.heritage.platform.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        String message = e.getMessage();

        if (message != null && message.contains("Too many attempts")) {
            return ApiResponse.error(429, message);
        }

        return ApiResponse.error(400, message);
    }
}