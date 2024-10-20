package com.meta.hackathon.queue;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.meta.hackathon.builder.send_message.SendMessage;
import com.meta.hackathon.config.ReloadableProperties;
import com.meta.hackathon.enums.Channel;
import com.meta.hackathon.enums.Verticals;
import com.meta.hackathon.model.IncomingMessage;
import com.meta.hackathon.util.TomcatUtils;
import com.meta.hackathon.vertical.reminder.ReminderImpl;
import com.meta.hackathon.vertical.reminder.SportsImpl;
import com.meta.hackathon.vertical.reminder.StikerImpl;
import com.meta.hackathon.vertical.reminder.TranslationImpl;
import com.meta.hackathon.vertical.reminder.TravelServiceImpl;
import com.meta.Queue.queue.Processor;

public class IncomingProcessor extends Processor<IncomingMessage> {
	public static final IncomingProcessor instance = new IncomingProcessor();
	private static final Logger LOG = LogManager.getLogger(IncomingProcessor.class.getName());

	private IncomingProcessor() {
		super(ReloadableProperties.getIncomingProcessorSleepTimeInMillis(),
				ReloadableProperties.getIncomingProcessorBatchSize(), true, true,
				ReloadableProperties.getIncomingProcessorNoOfThreads(), true);
	}

	@Override
	protected String getProcessorName() {
		return new StringBuilder(TomcatUtils.getTomcatName()).append("-")
				.append(IncomingProcessor.class.getSimpleName()).toString();
	}

	@Override
	protected void processEntries(List<IncomingMessage> incomingMessages) throws Exception {
		for (IncomingMessage incomingMessage : incomingMessages) {
			String grepString = incomingMessage.toGrepString();
			try {
				LOG.info("{}:::Processing => {}", grepString, incomingMessage);
				process(incomingMessage);
			} catch (Exception e) {
				LOG.error("Exception " + grepString, e);
			}
		}
	}

	private void process(IncomingMessage incomingMessage) throws Exception {
		incomingMessage.extractUserMessage();
		incomingMessage.identifyVertical();
		if (StringUtils.isBlank(incomingMessage.getVertical())
				|| incomingMessage.getVertical().equalsIgnoreCase("UNKNOWN")) {
			SendMessage sendMessage = new SendMessage();
			sendMessage.setChannel(Channel.WHATSAPP.name());
			sendMessage.setUserid(ReloadableProperties.getWhatsAppTwoWayAccountUserId());
			sendMessage.setPassword(ReloadableProperties.getWhatsAppTwoWayAccountPassword());
			sendMessage.setMethod("sendmessage");
			sendMessage.setSend_to(String.valueOf(incomingMessage.getMobile()));
			if (StringUtils.isNotBlank(incomingMessage.getLlmResponse())) {
				sendMessage.setMsg(incomingMessage.getLlmResponse());
			} else {
				sendMessage.setMsg("The vertical inferred from your query is not supported.");
			}
			sendMessage.setMsg_type("text");
			SendMessageProcessor.instance.addEntry(sendMessage);
		} else {
			if (Verticals.REMINDER.name().equalsIgnoreCase(incomingMessage.getVertical().trim())) {
				new ReminderImpl().process(incomingMessage);
			}
			if (Verticals.TRANSLATION.name().equalsIgnoreCase(incomingMessage.getVertical().trim())) {
				new TranslationImpl().process(incomingMessage);
			}
			if (Verticals.SPORTS.name().equalsIgnoreCase(incomingMessage.getVertical().trim())) {
				new SportsImpl().process(incomingMessage);
			}
			if (Verticals.TRAVEL.name().equalsIgnoreCase(incomingMessage.getVertical().trim())) {
				new TravelServiceImpl().process(incomingMessage);
			}
			if (Verticals.MEME.name().equalsIgnoreCase(incomingMessage.getVertical().trim())) {
				new StikerImpl().process(incomingMessage);
			}
		}
	}
}
