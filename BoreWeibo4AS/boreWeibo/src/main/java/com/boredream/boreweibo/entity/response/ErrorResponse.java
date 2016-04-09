package com.boredream.boreweibo.entity.response;

import android.text.TextUtils;

public class ErrorResponse {
	private String error;
	private String error_code;
	private String request;

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getError_code() {
		return error_code;
	}

	public void setError_code(String error_code) {
		this.error_code = error_code;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	@Override
	public String toString() {
		return "ErrorResponse [error=" + error + ", error_code=" + error_code + ", request=" + request + "]";
	}
	
	public boolean isNull() {
		return this == null || TextUtils.isEmpty(this.error);
	}
}
