package com.meta.hackathon.model;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.meta.hackathon.ai.LLM;
import com.meta.hackathon.config.ReloadableProperties;
import com.meta.hackathon.exception.ValidationException;
import com.meta.hackathon.factory.LLMFactory;
import com.meta.hackathon.http.HTTPResponse;
import com.meta.hackathon.util.Misc;
import com.meta.Queue.queue.Writable;

public class IncomingMessage extends Writable {

	private String name;
	private String payload;
	private long mobile;
	private String type;
	private String vertical;
	private String userMessage;
	private String audioFilePath;
	private String llmResponse;

	public IncomingMessage() {

	}

	public IncomingMessage(String payload) {
		this.payload = payload;
		JSONObject payloadObject = new JSONObject(payload);
		this.name = payloadObject.getString("name");
		this.mobile = Long.parseLong(payloadObject.getString("mobile"));
		this.type = payloadObject.getString("type");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPayload() {
		return payload;
	}

	public long getMobile() {
		return mobile;
	}

	public String getType() {
		return type;
	}

	public String getVertical() {
		return vertical;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public void setMobile(long mobile) {
		this.mobile = mobile;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setVertical(String vertical) {
		this.vertical = vertical;
	}

	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}

	public String getUserMessage() {
		return userMessage;
	}

	public String getAudioFilePath() {
		return audioFilePath;
	}

	public void setAudioFilePath(String audioFilePath) {
		this.audioFilePath = audioFilePath;
	}

	public String getLlmResponse() {
		return llmResponse;
	}

	public void setLlmResponse(String llmResponse) {
		this.llmResponse = llmResponse;
	}

	public void validate() throws ValidationException {
		if (!(this.type.equalsIgnoreCase("text") || this.type.equalsIgnoreCase("audio"))) {
			throw new ValidationException("Unsupported input type");
		}
	}

	public void extractUserMessage() throws Exception {
		this.userMessage = getUserMsg();
		if (StringUtils.isBlank(this.userMessage)) {
			throw new ValidationException("Unsupported input type");
		}
	}

	private String getUserMsg() throws Exception {
		if (this.type.equalsIgnoreCase("text")) {
			return getStringMessage();
		} else if (this.type.equalsIgnoreCase("audio")) {
			return getAudioMessage();
		}
		return null;
	}

	private String getStringMessage() {
		JSONObject payloadObject = new JSONObject(this.payload);
		return payloadObject.getString("text");
	}

	private String getAudioMessage() throws Exception {
		downloadAudioFile();
		convertSpeechToText();
		return this.userMessage;
	}

	private void downloadAudioFile() throws IOException {
		JSONObject payloadObject = new JSONObject(this.payload);
		JSONObject audioObject = new JSONObject(payloadObject.getString("audio"));
		String url = new StringBuilder(audioObject.getString("url")).append(audioObject.getString("signature"))
				.toString();
		String destinationFilePath = new StringBuilder(ReloadableProperties.getAudioFileDownloadLocation())
				.append("input_").append(System.currentTimeMillis()).append(".ogg").toString();
		Misc.downloadFile(url, destinationFilePath);
		this.audioFilePath = destinationFilePath;
	}

	private void convertSpeechToText() throws Exception {
		LLM llm = LLMFactory.instance.getLLM(ReloadableProperties.getDefaultLLMProvider(),
				ReloadableProperties.getGroqWhisperModel(), null);
		llm.setAudioFilePath(this.audioFilePath);
		HTTPResponse httpResponse = llm.performTranscription();
		if (httpResponse != null && httpResponse.isSuccessful()) {
			String response = httpResponse.getBodyAsString();
			if (StringUtils.isNotBlank(response)) {
				JSONObject llmResponseObject = new JSONObject(response);
				this.userMessage = llmResponseObject.getString("text").trim();
			}
		}
	}

	public void identifyVertical() throws Exception {
		String prompt = ReloadableProperties.getInferVerticalPrompt().replace("{{user_message}}", this.userMessage);
		LLM llm = LLMFactory.instance.getLLM(ReloadableProperties.getDefaultLLMProvider(),
				ReloadableProperties.getGroqLLMModel(), prompt);
		HTTPResponse httpResponse = llm.performChatCompletion();
		if (httpResponse != null && httpResponse.isSuccessful()) {
			String response = httpResponse.getBodyAsString();
			if (StringUtils.isNotBlank(response)) {
				JSONObject llmResponseObject = new JSONObject(response);
				JSONObject responseObject = llmResponseObject.getJSONArray("choices").getJSONObject(0);
				String verticalResponse = responseObject.getJSONObject("message").getString("content");
				JSONObject verticalResponseObject = new JSONObject(verticalResponse);
				this.vertical = verticalResponseObject.getString("vertical");
				if (this.vertical.equalsIgnoreCase("unknown")) {
					this.llmResponse = verticalResponseObject.optString("response", null);
				}
				return;
			}
		}
		this.vertical = "UNKNOWN";
	}

	public String toGrepString() {
		return new StringBuilder(String.valueOf(this.mobile)).append("::").append(this.vertical).append("::")
				.append(this.userMessage).toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IncomingMessage [name=");
		builder.append(name);
		builder.append(", payload=");
		builder.append(payload);
		builder.append(", mobile=");
		builder.append(mobile);
		builder.append(", type=");
		builder.append(type);
		builder.append(", vertical=");
		builder.append(vertical);
		builder.append(", userMessage=");
		builder.append(userMessage);
		builder.append(", audioFilePath=");
		builder.append(audioFilePath);
		builder.append("]");
		return builder.toString();
	}

}
