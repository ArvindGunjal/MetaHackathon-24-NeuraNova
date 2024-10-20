package com.meta.hackathon.http;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.meta.hackathon.util.ByteUtils;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HTTPResponse {
	private static final Logger LOG = LogManager.getLogger(HTTPResponse.class.getName());
	boolean success = false;
	Response response;
	byte[] body;

	public static HTTPResponse createFailedResponse(Response response) {
		HTTPResponse responseObject = new HTTPResponse();
		responseObject.response = response;
		try {
			responseObject.body = response.body().bytes();
		} catch (IOException e) {
			LOG.error("Exception ", e);
		}
		return responseObject;
	}

	public static HTTPResponse createSuccessResponse(Response response, byte[] body) {
		HTTPResponse responseObject = new HTTPResponse();
		responseObject.response = response;
		responseObject.body = body;
		responseObject.success = true;
		return responseObject;
	}

	private HTTPResponse() {
	}

	public byte[] getBody() {
		return body;
	}

	public String getBodyAsString() {
		if (success) {
			return body != null ? ByteUtils.toUTFString(body) : "";
		} else {
			return body != null ? ByteUtils.toUTFString(body) : getResponse().message();
		}
	}

	public Response getResponse() {
		return response;
	}

	public boolean isSuccessful() {
		return success;
	}

	public void close() throws IOException {
		if (response != null) {
			response.close();
		}
	}

	public static Response errorResponse(Exception e, Request request, long sentTs) {
		return new Response.Builder()//
				.message(e.getMessage()) //
				.code(503) //
				.receivedResponseAtMillis(System.currentTimeMillis()) //
				.request(request) //
				.protocol(Protocol.HTTP_1_1) //
				.sentRequestAtMillis(sentTs) //
				.body(ResponseBody.create(e.getMessage(), MediaType.parse("text/html"))) //
				.build();
	}

	// clone existing response mainly it will be used for error response
	public static Response cloneResponse(Response response) throws IOException {
		return new Response.Builder()//
				.message(response.message()) //
				.code(response.code()) //
				.networkResponse(response.networkResponse()) //
				.receivedResponseAtMillis(response.receivedResponseAtMillis()) //
				.request(response.request()) //
				.sentRequestAtMillis(response.sentRequestAtMillis()) //
				.protocol(response.protocol()) //
				.headers(response.headers()).body(cloneResponseBody(response)) //
				.build();
	}

	private static ResponseBody cloneResponseBody(okhttp3.Response rawResponse) throws IOException {
		final ResponseBody responseBody = rawResponse.body();
		return ResponseBody.create(responseBody.bytes(), responseBody.contentType());
	}

	@Override
	public String toString() {
		return "ResponseObject [success=" + success + ", response=" + response + "]";
	}

}
