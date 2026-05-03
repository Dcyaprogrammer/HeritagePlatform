package com.heritage.platform.exception;

import com.heritage.platform.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleRuntimeException() {
        RuntimeException ex = new RuntimeException("Standard Error");
        ApiResponse<Void> response = handler.handleRuntimeException(ex);
        assertEquals(400, response.getCode());
        assertEquals("Standard Error", response.getMessage());
    }

    @Test
    void shouldHandleRateLimitException() {
        RuntimeException ex = new RuntimeException("Too many attempts, try later");
        ApiResponse<Void> response = handler.handleRuntimeException(ex);
        assertEquals(429, response.getCode()); 
    }

    @Test
    void shouldHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("No Permission");
        ApiResponse<Void> response = handler.handleAccessDeniedException(ex);
        assertEquals(403, response.getCode());
    }

    @Test
    void shouldHandleGeneralException() {
        Exception ex = new Exception("Critical System Error");
        ApiResponse<Void> response = handler.handleException(ex);
        assertEquals(500, response.getCode()); 
    }
}