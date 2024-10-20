package com.meta.hackathon.ai.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.hackathon.ai.LLM;
import com.meta.hackathon.builder.chat_completion.ChatCompletion;
import com.meta.hackathon.builder.chat_completion.Messages;
import com.meta.hackathon.builder.chat_completion.ResponseFormat;
import com.meta.hackathon.config.ReloadableProperties;
import com.meta.hackathon.enums.PayloadType;
import com.meta.hackathon.enums.RequestMethod;
import com.meta.hackathon.http.HTTPRequest;
import com.meta.hackathon.http.HTTPResponse;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GroqLLM extends LLM {

	@Override
	public HTTPResponse performChatCompletion() throws Exception {
		String httpUrl = ReloadableProperties.getGroqLLMAPI();
		JSONObject headers = new JSONObject();
		ObjectMapper mapper = new ObjectMapper();
		headers.put("Authorization", ReloadableProperties.getGroqLLMAPIKey());
		HTTPRequest httpRequest = new HTTPRequest(httpUrl, RequestMethod.POST, headers, PayloadType.JSON,
				mapper.writeValueAsString(constructChatCompletionObject()));
		return httpRequest.execute();
	}

	@Override
	public HTTPResponse performTranscription() throws Exception {
		String httpUrl = ReloadableProperties.getGroqLLMTranscriptionAPI();
		JSONObject headers = new JSONObject();
		headers.put("Authorization", ReloadableProperties.getGroqLLMAPIKey());
		JSONObject payload = new JSONObject();
		payload.put("model", ReloadableProperties.getGroqWhisperModel());
		payload.put("file", this.audioFilePath);
		payload.put("response_format", "verbose_json");
		HTTPRequest httpRequest = new HTTPRequest(httpUrl, RequestMethod.POST, headers, PayloadType.FORMDATA,
				payload.toString());
		return httpRequest.execute();
	}

	@Override
	public HTTPResponse performTextToSpeech() throws Exception {
		return null;
	}

	private static Messages prepareMessage(String prompt) {
		Messages message = new Messages();
		message.setRole("user");
		message.setContent(prompt);
		return message;
	}

	private ChatCompletion constructChatCompletionObject() {
		List<Messages> allMessages = new ArrayList<>();
		allMessages.add(prepareMessage(this.prompt));
		ResponseFormat response_format = new ResponseFormat();
		response_format.setType("json_object");
		ChatCompletion.Builder chatCompletionBuilder = new ChatCompletion.Builder(this.model, allMessages)
				.maxTokens(1024).topP(1.0).temperature(this.temperature).setN(1).response_format(response_format);
		return chatCompletionBuilder.build();
	}

	@Override
	public HTTPResponse performTranslation() throws Exception {
		return null;
	}

}
