package com.meta.hackathon.vertical.reminder;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.meta.hackathon.ai.LLM;
import com.meta.hackathon.builder.send_message.SendMessage;
import com.meta.hackathon.config.ReloadableProperties;
import com.meta.hackathon.enums.Channel;
import com.meta.hackathon.factory.LLMFactory;
import com.meta.hackathon.http.HTTPResponse;
import com.meta.hackathon.model.IncomingMessage;
import com.meta.hackathon.queue.SendMessageProcessor;
import com.meta.hackathon.vertical.Vertical;

public class StikerImpl implements Vertical {

	private static final Logger LOG = LogManager.getLogger(StikerImpl.class.getSimpleName());

	@Override
	public void process(IncomingMessage incomingMessage) throws Exception {

		String commonExceptionMsg = "Something went wrong while processing request";

		try {

			String memesPrompt = ReloadableProperties.getMemesPrompt()
					.replace("{{description}}", ReloadableProperties.getMemesDescription().toString())
					.replace("{{user_prompt}}", incomingMessage.getUserMessage());

			JSONObject llmResponse = callLlmModel(incomingMessage, memesPrompt);

			if (llmResponse.getBoolean("is_description_match")) {
				if (llmResponse.has("file_name_list") && !llmResponse.getJSONArray("file_name_list").isEmpty()) {

					for (int i = 0; i < llmResponse.getJSONArray("file_name_list").length(); i++) {

						String memesUrl = ReloadableProperties.getMemesUrlHost()
								+ llmResponse.getJSONArray("file_name_list").getString(i);

						executeMediaApi(String.valueOf(incomingMessage.getMobile()), "", "sticker", memesUrl,
								"sendmediamessage");

					}
				} else {
					executeMediaApi(String.valueOf(incomingMessage.getMobile()),
							"No memes found. Please try with something else", "text", null, "sendmessage");
				}

			} else {
				executeMediaApi(String.valueOf(incomingMessage.getMobile()),
						"No memes found. Please try with something else", "text", null, "sendmessage");
			}

		} catch (Exception e) {
			LOG.error("exception while processing memes request", e);
			executeMediaApi(String.valueOf(incomingMessage.getMobile()), commonExceptionMsg, "text", null,
					"sendmessage");
		}

	}

	private JSONObject callLlmModel(IncomingMessage incomingMessage, String prompt) throws Exception {
		LLM llm = LLMFactory.instance.getLLM(ReloadableProperties.getDefaultLLMProvider(),
				ReloadableProperties.getGroqLLMModel(), prompt);
		HTTPResponse completionResponse = llm.performChatCompletion();
		JSONObject dataExtractionResponse = new JSONObject(new JSONObject(completionResponse.getBodyAsString())
				.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content"));
		LOG.info("{} - response from llm {}", incomingMessage.getMobile(), dataExtractionResponse);
		return dataExtractionResponse;
	}

	public void executeMediaApi(String mobile, String msg, String msgType, String mediaUrl, String methodType)
			throws UnsupportedEncodingException {

		SendMessage sendMessage = new SendMessage();
		sendMessage.setChannel(Channel.WHATSAPP.name());
		sendMessage.setUserid(ReloadableProperties.getWhatsAppTwoWayAccountUserId());
		sendMessage.setPassword(ReloadableProperties.getWhatsAppTwoWayAccountPassword());
		sendMessage.setMethod(methodType);
		sendMessage.setSend_to(String.valueOf(mobile));

		if (!StringUtils.isBlank(msg)) {
			sendMessage.setMsg(msg);
		}

		if (!StringUtils.isBlank(mediaUrl)) {
			sendMessage.setMedia_url(mediaUrl);
			sendMessage.setIsHSM("false");
		}

		sendMessage.setMsg_type(msgType);
		SendMessageProcessor.instance.addEntry(sendMessage);
	}
}
