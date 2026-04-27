package com.heritage.platform.web;

public class ApiEnvelope<T> {
    private int code;
    private String message;
    private T data;

    public ApiEnvelope() {
    }

    public ApiEnvelope(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiEnvelope<T> ok(T data) {
        return new ApiEnvelope<>(200, "success", data);
    }

    public static <T> ApiEnvelope<T> error(int code, String message) {
        return new ApiEnvelope<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
