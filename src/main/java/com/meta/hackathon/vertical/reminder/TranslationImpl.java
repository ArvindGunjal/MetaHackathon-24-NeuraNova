package com.meta.hackathon.vertical.reminder;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.hackathon.ai.LLM;
import com.meta.hackathon.builder.send_message.SendMessage;
import com.meta.hackathon.config.ReloadableProperties;
import com.meta.hackathon.enums.Channel;
import com.meta.hackathon.enums.PayloadType;
import com.meta.hackathon.enums.RequestMethod;
import com.meta.hackathon.enums.Verticals;
import com.meta.hackathon.factory.LLMFactory;
import com.meta.hackathon.http.HTTPRequest;
import com.meta.hackathon.http.HTTPResponse;
import com.meta.hackathon.model.IncomingMessage;
import com.meta.hackathon.model.vertical.translation.Translation;
import com.meta.hackathon.vertical.Vertical;

public class TranslationImpl implements Vertical {

	@Override
	public void process(IncomingMessage incomingMessage) throws Exception {
		Translation translation = extractParameters(incomingMessage);
		sendTranslation(incomingMessage, translation);
	}

	private Translation extractParameters(IncomingMessage incomingMessage) throws Exception {
		JSONObject verticalWisePrompts = new JSONObject(ReloadableProperties.getVerticalPrompts());
		String prompt = verticalWisePrompts.getString(Verticals.TRANSLATION.name().toLowerCase());
		prompt = prompt.replace("{{user_message}}", incomingMessage.getUserMessage());
		LLM llm = LLMFactory.instance.getLLM(ReloadableProperties.getDefaultLLMProvider(),
				ReloadableProperties.getGroqLLMModel(), prompt);
		llm.setTemperature(1.0);
		HTTPResponse completionResponse = llm.performChatCompletion();
		String reminderJSON = new JSONObject(new JSONObject(completionResponse.getBodyAsString())
				.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")).toString();
		return new ObjectMapper().readValue(reminderJSON, Translation.class);
	}

	private void sendTranslation(IncomingMessage incomingMessage, Translation translationObject) throws Exception {
		String translatedText = null;
		LLM llm = LLMFactory.instance.getLLM("sarvam", null, null);
		String translationPayload = prepareSarvamTranslationPayload(incomingMessage, translationObject);
		llm.setTranslationPayload(translationPayload);
		HTTPResponse translationResponse = llm.performTranslation();
		if (translationResponse != null && translationResponse.isSuccessful()) {
			JSONObject responseObject = new JSONObject(translationResponse.getBodyAsString());
			translatedText = responseObject.getString("translated_text");
			translationObject.setTranslatedText(translatedText);
		}
		if (translationObject.getOutputFormat().equalsIgnoreCase("text")) {
			SendMessage sendMessage = new SendMessage();
			sendMessage.setChannel(Channel.WHATSAPP.name());
			sendMessage.setMsg_type("text");
			sendMessage.setMsg(translatedText);
			sendMessage.setUserid(ReloadableProperties.getWhatsAppTwoWayAccountUserId());
			sendMessage.setPassword(ReloadableProperties.getWhatsAppTwoWayAccountPassword());
			sendMessage.setData_encoding("UNICODE_TEXT");
			sendMessage.setMethod("sendmessage");
			// sendMessage.setIsHSM("false");
			sendMessage.setSend_to(String.valueOf(incomingMessage.getMobile()));
			sendMessage.send();
		} else {
			String mediaUrl = prepareGoogleTTSMediaUrl(incomingMessage, translationObject);
			SendMessage sendMessage = new SendMessage();
			sendMessage.setChannel(Channel.WHATSAPP.name());
			sendMessage.setMsg_type("AUDIO");
			sendMessage.setMedia_url(mediaUrl);
			sendMessage.setUserid(ReloadableProperties.getWhatsAppTwoWayAccountUserId());
			sendMessage.setPassword(ReloadableProperties.getWhatsAppTwoWayAccountPassword());
			sendMessage.setMethod("sendmediamessage");
			sendMessage.setIsHSM("false");
			sendMessage.setSend_to(String.valueOf(incomingMessage.getMobile()));
			sendMessage.send();
		}

	}

	private String prepareGoogleTTSPayload(IncomingMessage incomingMessage, Translation translationObject)
			throws JSONException {
		JSONObject payloadObject = new JSONObject();
		JSONObject inputObject = new JSONObject();
		inputObject.put("text", translationObject.getTranslatedText());
		JSONObject voiceObject = new JSONObject();
		voiceObject.put("languageCode", translationObject.getTargetLanguageCode());
		voiceObject.put("name",
				new StringBuilder(translationObject.getTargetLanguageCode()).append("-IN-Standard-A").toString());
		voiceObject.put("ssmlGender", "FEMALE");
		JSONObject audioConfigObject = new JSONObject();
		audioConfigObject.put("audioEncoding", "MP3");
		audioConfigObject.put("pitch", 1.20);
		audioConfigObject.put("speakingRate", 0.90);
		payloadObject.put("input", inputObject);
		payloadObject.put("voice", voiceObject);
		payloadObject.put("audioConfig", audioConfigObject);
		return payloadObject.toString();

	}

	private String prepareSarvamTTSPayload(IncomingMessage incomingMessage, Translation translationObject)
			throws JSONException {
		JSONObject payloadObject = new JSONObject();
		JSONArray inputs = new JSONArray();
		inputs.put(translationObject.getTranslatedText());
		payloadObject.put("inputs", inputs);
		payloadObject.put("target_language_code",
				new StringBuilder(translationObject.getTargetLanguageCode()).append("-IN"));
		payloadObject.put("speaker", "meera");
		payloadObject.put("pitch", 0);
		payloadObject.put("pace", 1.1);
		payloadObject.put("loudness", 2);
		payloadObject.put("speech_sample_rate", 8000);
		payloadObject.put("enable_preprocessing", true);
		payloadObject.put("model", "bulbul:v1");
		return payloadObject.toString();
	}

	private String prepareSarvamTranslationPayload(IncomingMessage incomingMessage, Translation translationObject)
			throws JSONException {
		JSONObject inputObject = new JSONObject();
		inputObject.put("input", translationObject.getTextToBeTranslated());
		inputObject.put("source_language_code",
				new StringBuilder(translationObject.getSourceLanguageCode()).append("-IN").toString());
		inputObject.put("target_language_code",
				new StringBuilder(translationObject.getTargetLanguageCode()).append("-IN").toString());
		inputObject.put("speaker_gender", "Male");
		inputObject.put("mode", "code-mixed");
		inputObject.put("model", "mayura:v1");
		inputObject.put("enable_preprocessing", true);
		return inputObject.toString();

	}

	private String upload(String audioContent, String fileName) throws Exception {
		String uploadAPI = new StringBuilder(ReloadableProperties.getTTSMp3FileUploadAPI()).append("?fileName=")
				.append(fileName).toString();
		HTTPResponse httpResponse = new HTTPRequest(uploadAPI, RequestMethod.POST, null, PayloadType.TEXT, audioContent)
				.execute();
		if (httpResponse != null && httpResponse.isSuccessful()) {
			String mp3FileUrl = httpResponse.getBodyAsString();
			if (StringUtils.isNotBlank(mp3FileUrl)) {
				return mp3FileUrl;
			} else {
				throw new Exception("An unknown exception has occurred. Please retry the request after some time.");
			}
		}
		return null;
	}

	private String prepareGoogleTTSMediaUrl(IncomingMessage incomingMessage, Translation translationObject)
			throws Exception {
		LLM llm = LLMFactory.instance.getLLM("google", null, null);
		llm.setTtsPayload(prepareGoogleTTSPayload(incomingMessage, translationObject));
		HTTPResponse ttsResponse = llm.performTextToSpeech();
		if (ttsResponse != null && ttsResponse.isSuccessful()) {
			JSONObject responseObject = new JSONObject(ttsResponse.getBodyAsString());
			String audioContent = responseObject.getString("audioContent");
			String mp3MediaUrl = upload(audioContent, new StringBuilder("meta_hackathon_2024_translation_")
					.append(System.currentTimeMillis()).append(".mp3").toString());
			if (StringUtils.isNotBlank(mp3MediaUrl)) {
				return mp3MediaUrl;
			}
		}
		return null;
	}

	private String prepareSarvamTTSMediaUrl(IncomingMessage incomingMessage, Translation translationObject)
			throws Exception {
		LLM sarvamLLM = LLMFactory.instance.getLLM("sarvam", null, null);
		String ttsPayload = prepareSarvamTTSPayload(incomingMessage, translationObject);
		sarvamLLM.setTtsPayload(ttsPayload);
		HTTPResponse ttsResponse = sarvamLLM.performTextToSpeech();
		if (ttsResponse != null && ttsResponse.isSuccessful()) {
			JSONObject responseObject = new JSONObject(ttsResponse.getBodyAsString());
			JSONArray ttsAudios = responseObject.getJSONArray("audios");
			String audioContent = ttsAudios.getString(0);
			String mp3MediaUrl = upload(audioContent, new StringBuilder("meta_hackathon_2024_translation_")
					.append(System.currentTimeMillis()).append(".wav").toString());
			if (StringUtils.isNotBlank(mp3MediaUrl)) {
				return mp3MediaUrl;
			}
		}
		return null;
	}

}
