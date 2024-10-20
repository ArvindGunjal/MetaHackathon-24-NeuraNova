package com.meta.hackathon.enums;

public enum PayloadType {

	JSON("application/json"), TEXT("text/plain"), QUERYPARAMS(""), FORMDATA("multipart/form-data"),
	FORMURLENCODED("application/x-www-form-urlencoded");

	private final String contentType;

	private PayloadType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

}
