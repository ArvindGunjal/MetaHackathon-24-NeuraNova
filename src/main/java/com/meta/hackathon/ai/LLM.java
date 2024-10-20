package com.meta.hackathon.ai;

import com.meta.hackathon.http.HTTPResponse;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class LLM {

	protected String model;
	protected String prompt;
	protected String audioFilePath;
	protected String languageCode;
	protected String ttsPayload;
	protected String translationPayload;
	protected double temperature = 0.0;

	public abstract HTTPResponse performChatCompletion() throws Exception;

	public abstract HTTPResponse performTranscription() throws Exception;

	public abstract HTTPResponse performTextToSpeech() throws Exception;

	public abstract HTTPResponse performTranslation() throws Exception;

}
