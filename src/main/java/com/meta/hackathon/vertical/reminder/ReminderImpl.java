package com.meta.hackathon.vertical.reminder;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.hackathon.ai.LLM;
import com.meta.hackathon.builder.send_message.SendMessage;
import com.meta.hackathon.config.ReloadableProperties;
import com.meta.hackathon.enums.Channel;
import com.meta.hackathon.enums.Verticals;
import com.meta.hackathon.factory.LLMFactory;
import com.meta.hackathon.http.HTTPResponse;
import com.meta.hackathon.model.IncomingMessage;
import com.meta.hackathon.model.vertical.reminder.Reminder;
import com.meta.hackathon.util.Misc;
import com.meta.hackathon.vertical.Vertical;

public class ReminderImpl implements Vertical {

	@Override
	public void process(IncomingMessage incomingMessage) throws Exception {
		Reminder reminder = extractParameters(incomingMessage);
		sendReminder(incomingMessage, reminder);

		SendMessage sendMessage = new SendMessage();
		sendMessage.setChannel(Channel.WHATSAPP.name());
		sendMessage.setMsg_type("text");
		sendMessage.setMsg("Ok, will remind you about this..");
		sendMessage.setUserid(ReloadableProperties.getWhatsAppTwoWayAccountUserId());
		sendMessage.setPassword(ReloadableProperties.getWhatsAppTwoWayAccountPassword());
		sendMessage.setMethod("sendmessage");
		sendMessage.setSend_to(String.valueOf(incomingMessage.getMobile()));
		sendMessage.send();
	}

	private Reminder extractParameters(IncomingMessage incomingMessage) throws Exception {
		JSONObject verticalWisePrompts = new JSONObject(ReloadableProperties.getVerticalPrompts());
		String prompt = verticalWisePrompts.getString(Verticals.REMINDER.name().toLowerCase());
		prompt = prompt.replace("{{currentTimestampInyyyyMMddHHmmss}}", Misc.getCurrentTime());
		prompt = prompt.replace("{{user_message}}", incomingMessage.getUserMessage());
		LLM llm = LLMFactory.instance.getLLM(ReloadableProperties.getDefaultLLMProvider(),
				ReloadableProperties.getGroqLLMModel(), prompt);
		HTTPResponse completionResponse = llm.performChatCompletion();
//		JSONObject reminderParameters = new JSONObject(new JSONObject(completionResponse.getBodyAsString())
//				.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content"));
		String reminderJSON = new JSONObject(new JSONObject(completionResponse.getBodyAsString())
				.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")).toString();
		return new ObjectMapper().readValue(reminderJSON, Reminder.class);
	}

	private void sendReminder(IncomingMessage incomingMessage, Reminder reminderObject) {
		String whenToRemind = reminderObject.getReminder_timestamp();
		String whereToRemind = reminderObject.getChannel();
		Channel channel = Channel.valueOf(whereToRemind.trim().toUpperCase());
		if (channel == Channel.EMAIL) {
			SendMessage sendMessage = new SendMessage();
			sendMessage.setChannel(Channel.EMAIL.name());
			sendMessage.setSubject("Reminder Alert!");
			sendMessage.setContent(prepareReminderMessage(incomingMessage, reminderObject));
			sendMessage.setContent_type("text/html");
			sendMessage.setUserid(ReloadableProperties.getEmailAccountUserId());
			sendMessage.setPassword(ReloadableProperties.getEmailAccountPassword());
			sendMessage.setMethod("ems_post_campaign");
			if (StringUtils.isNotBlank(reminderObject.getEmail_address())) {
				sendMessage.setRecipients(reminderObject.getEmail_address());
			} else {
				sendMessage.setRecipients("abc@gmail.com");
			}
			sendMessage.setScheduled_at(whenToRemind);
			sendMessage.setName("Jarvis Over WhatsApp");
			// sendMessage.setFromEmailId("jarvis");
			sendMessage.send();
		} else if (channel == Channel.SMS) {
			SendMessage sendMessage = new SendMessage();
			sendMessage.setChannel(Channel.SMS.name());
			sendMessage.setDltTemplateId(ReloadableProperties.getReminderSMSMessageTemplateID());
			sendMessage.setMsg_type("unicode_text");
			String reminderMsgTemplate = ReloadableProperties.getReminderSMSMessageTemplate();
			reminderMsgTemplate = reminderMsgTemplate.replace("{{user}}", incomingMessage.getName())
					.replace("{{reminder_text}}", reminderObject.getText());
//					.replace("{{bot_link}}", new StringBuilder("https://wa.me/")
//							.append(ReloadableProperties.getWhatsAppWABANumber()).append("?text=Hello").toString());
			sendMessage.setMsg(reminderMsgTemplate);
			sendMessage.setUserid(ReloadableProperties.getSMSAccountUserId());
			sendMessage.setPassword(ReloadableProperties.getSMSAccountPassword());
			sendMessage.setMethod("sendmessage");
			sendMessage.setSend_to(String.valueOf(incomingMessage.getMobile()));
			sendMessage.setTimestamp(whenToRemind);
			sendMessage.send();
		} else if (channel == Channel.WHATSAPP) {
			SendMessage sendMessage = new SendMessage();
			sendMessage.setChannel(Channel.WHATSAPP.name());
			sendMessage.setTemplate_id(ReloadableProperties.getReminderWhatsAppMessageTemplateID());
			Map<String, String> variables = new HashMap<>();
			variables.put("var1", incomingMessage.getName());
			variables.put("var2", new StringBuilder("*").append(reminderObject.getText()).append("*").toString());
			variables.put("var3", new StringBuilder("https://wa.me/")
					.append(ReloadableProperties.getWhatsAppWABANumber()).append("?text=Hello").toString());
			sendMessage.setVariables(variables);
			sendMessage.setMsg_type("text");
			sendMessage.setUserid(ReloadableProperties.getWhatsAppOneWayAccountUserId());
			sendMessage.setPassword(ReloadableProperties.getWhatsAppOneWayAccountPassword());
			sendMessage.setMethod("sendmessage");
			sendMessage.setSend_to(String.valueOf(incomingMessage.getMobile()));
			sendMessage.setTimestamp(whenToRemind);
			sendMessage.send();
		}

	}

	private String prepareReminderMessage(IncomingMessage incomingMessage, Reminder reminderObject) {
		String reminderMsgTemplate = ReloadableProperties.getReminderEmailMessage();
		reminderMsgTemplate = reminderMsgTemplate.replace("{{user}}", incomingMessage.getName())
				.replace("{{reminder_text}}", reminderObject.getText())
				.replace("{{bot_link}}", new StringBuilder("https://wa.me/")
						.append(ReloadableProperties.getWhatsAppWABANumber()).append("?text=Hello").toString());
		return reminderMsgTemplate;

	}

}
