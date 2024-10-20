package com.meta.hackathon.ai.impl;

import com.meta.hackathon.ai.LLM;
import com.meta.hackathon.config.ReloadableProperties;
import com.meta.hackathon.enums.PayloadType;
import com.meta.hackathon.enums.RequestMethod;
import com.meta.hackathon.http.HTTPRequest;
import com.meta.hackathon.http.HTTPResponse;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GoogleLLM extends LLM {

	@Override
	public HTTPResponse performChatCompletion() throws Exception {
		return null;
	}

	@Override
	public HTTPResponse performTranscription() throws Exception {
		return null;
	}

	@Override
	public HTTPResponse performTextToSpeech() throws Exception {
		HTTPRequest httpRequest = new HTTPRequest(
				new StringBuilder(ReloadableProperties.getGoogleTTSAPI()).append("?key=")
						.append(ReloadableProperties.getGoogleTTSAPIKey()).toString(),
				RequestMethod.POST, null, PayloadType.JSON, this.ttsPayload);
		httpRequest.setShouldLogResponseBody(false);
		return httpRequest.execute();
	}

	@Override
	public HTTPResponse performTranslation() throws Exception {
		return null;
	}

}
