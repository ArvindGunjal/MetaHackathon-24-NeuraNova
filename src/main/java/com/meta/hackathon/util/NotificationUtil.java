package com.meta.hackathon.util;

import com.meta.hackathon.builder.send_message.SendMessage;
import com.meta.hackathon.config.ReloadableProperties;
import com.meta.hackathon.enums.Channel;
import com.meta.hackathon.queue.SendMessageProcessor;

public class NotificationUtil {

	public static void sendWhatsAppMessage(SendMessage sendMessage) {
		SendMessageProcessor.instance.addEntry(sendMessage);
	}

	public static SendMessage prepareWhatsAppMessage(long mobile, String msg) {

		SendMessage sendMessage = new SendMessage();
		sendMessage.setChannel(Channel.WHATSAPP.name());
		sendMessage.setUserid(ReloadableProperties.getWhatsAppOneWayAccountUserId());
		sendMessage.setPassword(ReloadableProperties.getWhatsAppOneWayAccountPassword());
		sendMessage.setMethod("sendmessage");
		sendMessage.setSend_to(String.valueOf(mobile));
		sendMessage.setMsg(msg);
		sendMessage.setMsg_type("text");
		return sendMessage;
	}

}
