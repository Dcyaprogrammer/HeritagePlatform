package com.heritage.platform.web;

public class ApiEnvelope<T> {

	private int code;
	private String message;
	private T data;

	public static <T> ApiEnvelope<T> ok(T data) {
		ApiEnvelope<T> response = new ApiEnvelope<>();
		response.setCode(200);
		response.setMessage("success");
		response.setData(data);
		return response;
	}

	public static <T> ApiEnvelope<T> error(int code, String message) {
		ApiEnvelope<T> response = new ApiEnvelope<>();
		response.setCode(code);
		response.setMessage(message);
		response.setData(null);
		return response;
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
