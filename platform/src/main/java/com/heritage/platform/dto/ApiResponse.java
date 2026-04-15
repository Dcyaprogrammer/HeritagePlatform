package com.heritage.platform.dto;

/**
 * Unified API Response Wrapper / 统一API响应包装器
 * Standard response format for all API endpoints
 * 所有API端点的标准响应格式
 */
public class ApiResponse<T> {

	private int code;
	private String message;
	private T data;

	// Success response / 成功响应
	public static <T> ApiResponse<T> success(T data) {
		ApiResponse<T> response = new ApiResponse<>();
		response.setCode(200);
		response.setMessage("success");
		response.setData(data);
		return response;
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		ApiResponse<T> response = new ApiResponse<>();
		response.setCode(200);
		response.setMessage(message);
		response.setData(data);
		return response;
	}

	// Error response / 错误响应
	public static <T> ApiResponse<T> error(int code, String message) {
		ApiResponse<T> response = new ApiResponse<>();
		response.setCode(code);
		response.setMessage(message);
		return response;
	}

	public static <T> ApiResponse<T> error(String message) {
		return error(500, message);
	}

	// Getters and Setters

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
