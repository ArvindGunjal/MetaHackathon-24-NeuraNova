package com.meta.hackathon.api.service.incoming.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.meta.hackathon.api.service.incoming.IncomingService;
import com.meta.hackathon.builder.send_message.SendMessage;
import com.meta.hackathon.config.ReloadableProperties;
import com.meta.hackathon.enums.Channel;
import com.meta.hackathon.exception.ValidationException;
import com.meta.hackathon.model.IncomingMessage;
import com.meta.hackathon.queue.IncomingProcessor;
import com.meta.hackathon.queue.SendMessageProcessor;
import com.meta.hackathon.util.Misc;

public class IncomingServiceImpl implements IncomingService {
	private static final Logger LOG = LogManager.getLogger(IncomingServiceImpl.class.getSimpleName());
	public static final IncomingServiceImpl instance = new IncomingServiceImpl();

	@Override
	public void processIncoming(String incomingPayload) throws Exception {
		IncomingMessage incomingMessage = new IncomingMessage(incomingPayload);
		try {
			incomingMessage.validate();
			sendAck(incomingMessage);
			startProcessing(incomingMessage);
		} catch (ValidationException e) {
			SendMessage sendMessage = new SendMessage();
			sendMessage.setChannel(Channel.WHATSAPP.name());
			sendMessage.setUserid(ReloadableProperties.getWhatsAppTwoWayAccountUserId());
			sendMessage.setPassword(ReloadableProperties.getWhatsAppTwoWayAccountPassword());
			sendMessage.setIsHSM("false");
			sendMessage.setMethod("sendmessage");
			sendMessage.setSend_to(String.valueOf(incomingMessage.getMobile()));
			sendMessage.setMsg("Only text and audio input is supported as of now.");
			sendMessage.setMsg_type("text");
			SendMessageProcessor.instance.addEntry(sendMessage);
		}
	}

	private void startProcessing(IncomingMessage incomingMessage) {
		IncomingProcessor.instance.addEntry(incomingMessage);
	}

	private void sendAck(IncomingMessage incomingMessage) {
		JSONObject ackMsgs = ReloadableProperties.getAckMessages().getJSONObject("ackMessages");
		String ackMsg = null;
		if (incomingMessage.getType().equalsIgnoreCase("text")) {
			JSONArray textAckMsgs = ackMsgs.getJSONArray("text-input");
			ackMsg = textAckMsgs.getString(Misc.generateRandomNumber(0, textAckMsgs.length() - 1));
		} else {
			JSONArray audioAckMsgs = ackMsgs.getJSONArray("audio-input");
			ackMsg = audioAckMsgs.getString(Misc.generateRandomNumber(0, audioAckMsgs.length() - 1));
		}

		SendMessage sendMessage = new SendMessage();
		sendMessage.setChannel(Channel.WHATSAPP.name());
		sendMessage.setUserid(ReloadableProperties.getWhatsAppTwoWayAccountUserId());
		sendMessage.setPassword(ReloadableProperties.getWhatsAppTwoWayAccountPassword());
		// sendMessage.setIsHSM("false");
		sendMessage.setMethod("sendmessage");
		sendMessage.setSend_to(String.valueOf(incomingMessage.getMobile()));
		sendMessage.setMsg(ackMsg);
		sendMessage.setMsg_type("text");
		SendMessageProcessor.instance.addEntry(sendMessage);

	}
}
