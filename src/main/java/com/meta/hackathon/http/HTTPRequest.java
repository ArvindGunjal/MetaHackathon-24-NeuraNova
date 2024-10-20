package com.meta.hackathon.http;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.meta.hackathon.enums.PayloadType;
import com.meta.hackathon.enums.RequestMethod;
import com.meta.hackathon.util.OkHttpUtils;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

public class HTTPRequest {

	private static final Logger LOG = LogManager.getLogger(HTTPRequest.class.getName());
	private static final String CONTENTTYPE = "Content-Type";
	private static final String DELIMITER = ":::";

	private String traceId = UUID.randomUUID().toString();
	private String httpUrl;
	private RequestMethod httpMethod;
	private JSONObject headers;
	private PayloadType payloadType;
	private String payload;
	private Instant requestStartTime;
	private Instant requestEndTime;
	private String logDescription = "";
	private int retryCount;
	private boolean shouldLogRequestResponse = true;
	private boolean shouldLogResponseBody = true;

	public HTTPRequest(String traceId, String httpUrl, RequestMethod httpMethod, JSONObject headers,
			PayloadType payloadType, String payload) {
		this.traceId = traceId;
		this.httpUrl = httpUrl;
		this.httpMethod = httpMethod;
		this.headers = headers;
		this.payloadType = payloadType;
		this.payload = payload;
	}

	public HTTPRequest(String httpUrl, RequestMethod httpMethod, JSONObject headers, PayloadType payloadType,
			String payload) {
		this.httpUrl = httpUrl;
		this.httpMethod = httpMethod;
		this.headers = headers;
		this.payloadType = payloadType;
		this.payload = payload;
	}
	
	public HTTPRequest(String httpUrl, RequestMethod httpMethod,  PayloadType payloadType,
			String payload) {
		this.httpUrl = httpUrl;
		this.httpMethod = httpMethod;
		this.payloadType = payloadType;
		this.payload = payload;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getHttpUrl() {
		return httpUrl;
	}

	public void setHttpUrl(String httpUrl) {
		this.httpUrl = httpUrl;
	}

	public RequestMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(RequestMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public JSONObject getHeaders() {
		return headers;
	}

	public void setHeaders(JSONObject headers) {
		this.headers = headers;
	}

	public PayloadType getPayloadType() {
		return payloadType;
	}

	public void setPayloadType(PayloadType payloadType) {
		this.payloadType = payloadType;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public Instant getRequestStartTime() {
		return requestStartTime;
	}

	public void setRequestStartTime(Instant requestStartTime) {
		this.requestStartTime = requestStartTime;
	}

	public Instant getRequestEndTime() {
		return requestEndTime;
	}

	public void setRequestEndTime(Instant requestEndTime) {
		this.requestEndTime = requestEndTime;
	}

	public String getLogDescription() {
		return logDescription;
	}

	public void setLogDescription(String logDescription) {
		this.logDescription = logDescription;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public boolean shouldLogRequestResponse() {
		return shouldLogRequestResponse;
	}

	public void setShouldLogRequestResponse(boolean shouldLogRequestResponse) {
		this.shouldLogRequestResponse = shouldLogRequestResponse;
	}

	public boolean shouldLogResponseBody() {
		return shouldLogResponseBody;
	}

	public void setShouldLogResponseBody(boolean shouldLogResponseBody) {
		this.shouldLogResponseBody = shouldLogResponseBody;
	}

	public HTTPResponse execute() {
		HTTPResponse response = null;
		try {
			Request request = toOkHttpRequest();
			this.requestStartTime = Instant.now();
			if (this.shouldLogRequestResponse)
				LOG.info(prepareRequestLog());
			if (this.retryCount > 0) {
				response = OkHttpUtils.executeRequest(request, this.retryCount);
			} else {
				response = OkHttpUtils.executeRequest(request);
			}
			this.requestEndTime = Instant.now();
			if (this.shouldLogRequestResponse)
				LOG.info(prepareResponseLog(response));
			return response;
		} catch (Exception e) {
			LOG.error(prepareExceptionLog(e));
		}
		return response;
	}

	private String extractLogPayload(Request request) {
		try (Buffer buffer = new Buffer()) {
			final Request copy = request.newBuilder().build();
			copy.body().writeTo(buffer);
			return buffer.readUtf8();
		} catch (Exception ex) {
			LOG.error("Exception ", ex);
		}
		return null;
	}

	public Request toOkHttpRequest() throws JSONException {
		if (this.httpMethod == RequestMethod.GET) {
			return prepareGetRequest();
		} else if (this.httpMethod == RequestMethod.HEAD) {
			return prepareHeadRequest();
		} else if (this.httpMethod == RequestMethod.POST) {
			if (this.payloadType == PayloadType.FORMURLENCODED) {
				return preparePostRequest();
			} else if (this.payloadType == PayloadType.FORMDATA) {
				return preparePostRequest();
			} else if (this.payloadType == PayloadType.JSON) {
				RequestBody requestBody = RequestBody.create(this.payload,
						MediaType.parse(PayloadType.JSON.getContentType()));
				return preparePostRequest(requestBody);
			} else if (this.payloadType == PayloadType.TEXT) {
				RequestBody requestBody = RequestBody.create(this.payload,
						MediaType.parse(PayloadType.TEXT.getContentType()));
				return preparePostRequest(requestBody);
			} else {
				return preparePostRequest();
			}
		}
		return null;
	}

	private Request prepareGetRequest() throws JSONException {
		Headers headers = prepareHeaders();
		if (StringUtils.isNotBlank(this.payload)) {
			String queryParams = prepareQueryParams();
			StringBuilder urlWithQueryParams = new StringBuilder(this.httpUrl).append("?").append(queryParams);
			return new Request.Builder().url(urlWithQueryParams.toString()).headers(headers).get().build();
		} else {
			return new Request.Builder().url(this.httpUrl).headers(headers).get().build();
		}
	}

	private Request prepareHeadRequest() throws JSONException {
		return new Request.Builder().url(this.httpUrl).head().build();
	}

	private Request preparePostRequest() throws JSONException {
		Headers headers = prepareHeaders();
		return new Request.Builder().url(this.httpUrl).headers(headers)
				.post(this.payloadType == PayloadType.FORMDATA ? prepareMultipartBody() : prepareFormBody()).build();
	}

	private Request preparePostRequest(RequestBody requestBody) throws JSONException {
		Headers headers = prepareHeaders();
		return new Request.Builder().url(this.httpUrl).headers(headers).post(requestBody).build();
	}

	private FormBody prepareFormBody() throws JSONException {
		FormBody.Builder formBuilder = new FormBody.Builder();
		JSONObject bodyObject = new JSONObject(this.payload);
		if (bodyObject.length() > 0) {
			Iterator<String> keys = bodyObject.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				formBuilder.add(key, bodyObject.getString(key));
			}
		}
		return formBuilder.build();
	}

	private MultipartBody prepareMultipartBody() throws JSONException {
		MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
		JSONObject bodyObject = new JSONObject(this.payload);
		if (bodyObject.length() > 0) {
			Iterator<String> keys = bodyObject.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				if (key.equalsIgnoreCase("file")) {
					File file = new File(bodyObject.getString(key));
					multipartBuilder.addFormDataPart("file", file.getName(),
							RequestBody.create(file, MediaType.parse("application/octet-stream")));
				} else {
					multipartBuilder.addFormDataPart(key, bodyObject.getString(key));
				}
			}
		}
		return multipartBuilder.build();
	}

	private String prepareQueryParams() throws JSONException {
		StringBuilder queryParamsBuilder = new StringBuilder();
		JSONObject payload = new JSONObject(this.payload);
		Iterator<String> payloadKeys = payload.keys();
		while (payloadKeys.hasNext()) {
			String key = payloadKeys.next();
			queryParamsBuilder = queryParamsBuilder.append(key).append("=").append(payload.getString(key));
			if (payloadKeys.hasNext()) {
				queryParamsBuilder = queryParamsBuilder.append("&");
			}
		}
		return queryParamsBuilder.toString();
	}

	private Headers prepareHeaders() throws JSONException {
		Headers.Builder headerBuilder = new Headers.Builder();
		headerBuilder.addAll(prepareContentTypeHeader());
		Headers customHeaders = prepareCustomHeaders();
		if (customHeaders != null)
			headerBuilder.addAll(customHeaders);

		return headerBuilder.build();
	}

	private Headers prepareContentTypeHeader() {
		Headers.Builder headerBuilder = new Headers.Builder();
		if (this.payloadType == PayloadType.FORMURLENCODED) {
			headerBuilder.add(CONTENTTYPE, PayloadType.FORMURLENCODED.getContentType());
		} else if (this.payloadType == PayloadType.FORMDATA) {
			headerBuilder.add(CONTENTTYPE, PayloadType.FORMDATA.getContentType());
		} else if (this.payloadType == PayloadType.JSON) {
			headerBuilder.add(CONTENTTYPE, PayloadType.JSON.getContentType());
		}
		return headerBuilder.build();
	}

	private Headers prepareCustomHeaders() throws JSONException {
		if (this.headers != null && this.headers.length() > 0) {
			Headers.Builder headerBuilder = new Headers.Builder();
			Iterator<String> headerKeys = this.headers.keys();
			while (headerKeys.hasNext()) {
				String headerKey = headerKeys.next();
				headerBuilder.add(headerKey, this.headers.getString(headerKey));
			}
			return headerBuilder.build();
		}
		return null;
	}

	private String prepareRequestLog() {
		StringBuilder requestLogBuilder = new StringBuilder();
		requestLogBuilder.append("HTTP_REQUEST").append(DELIMITER).append(this.logDescription).append(DELIMITER)
				.append(prepareCommonLog());
		return requestLogBuilder.toString();
	}

	private String prepareExceptionLog(Exception ex) {
		StringBuilder exceptionLogBuilder = new StringBuilder();
		exceptionLogBuilder.append("HTTP_EXCEPTION").append(DELIMITER).append("Exception for ")
				.append(this.logDescription).append(DELIMITER).append(prepareCommonLog()).append(DELIMITER)
				.append("Err").append(":").append(ex.getMessage());
		return exceptionLogBuilder.toString();
	}

	private String prepareResponseLog(HTTPResponse response) {
		StringBuilder responseLogBuilder = new StringBuilder();
		responseLogBuilder = responseLogBuilder.append("HTTP_RESPONSE").append(DELIMITER);
		if (StringUtils.isNotBlank(this.logDescription)) {
			responseLogBuilder = responseLogBuilder.append("Response for ").append(this.logDescription)
					.append(DELIMITER);
		}
		responseLogBuilder = responseLogBuilder.append(prepareCommonLog()).append(DELIMITER).append("responseCode")
				.append(":").append(response.getResponse().code()).append(DELIMITER);
		if (this.shouldLogResponseBody) {
			responseLogBuilder = responseLogBuilder.append("responseBody:").append(response.getBodyAsString())
					.append(DELIMITER);
		}
		responseLogBuilder = responseLogBuilder.append("timeTakenInMs").append(":")
				.append(Duration.between(this.requestStartTime, this.requestEndTime).toMillis());
		return responseLogBuilder.toString();
	}

	private String prepareCommonLog() {
		StringBuilder commonLogBuilder = new StringBuilder();
		commonLogBuilder = commonLogBuilder.append(this.traceId).append(DELIMITER).append("url").append(":")
				.append(this.httpUrl).append(DELIMITER).append("httpMethod").append(":").append(this.httpMethod)
				.append(DELIMITER).append("headers").append(":").append(this.headers).append(DELIMITER)
				.append("payloadType").append(":").append(this.payloadType).append(DELIMITER).append("payload")
				.append(DELIMITER).append(this.payload);
		return commonLogBuilder.toString();
	}

}
