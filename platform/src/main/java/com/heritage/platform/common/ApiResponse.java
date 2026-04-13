//API统一响应




package com.heritage.platform.common;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor



public class ApiResponse<T> {


    private int code;
    private String message;
    private T data;
    //状态码 信息字段


    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}