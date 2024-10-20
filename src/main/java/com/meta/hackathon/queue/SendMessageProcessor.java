package com.meta.hackathon.queue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.meta.hackathon.builder.send_message.SendMessage;
import com.meta.hackathon.config.ReloadableProperties;
import com.meta.hackathon.util.TomcatUtils;
import com.meta.Queue.queue.Processor;

public class SendMessageProcessor extends Processor<SendMessage> {
	public static final SendMessageProcessor instance = new SendMessageProcessor();
	private static final Logger LOG = LogManager.getLogger(SendMessageProcessor.class.getName());

	private SendMessageProcessor() {
		super(ReloadableProperties.getSendMessageProcessorSleepTimeInMillis(),
				ReloadableProperties.getSendMessageProcessorBatchSize(), true, true,
				ReloadableProperties.getSendMessageProcessorNoOfThreads(), true);
	}

	@Override
	protected String getProcessorName() {
		return new StringBuilder(TomcatUtils.getTomcatName()).append("-")
				.append(SendMessageProcessor.class.getSimpleName()).toString();
	}

	@Override
	protected void processEntries(List<SendMessage> sendMsgTuples) throws Exception {
		for (SendMessage sendMsgTuple : sendMsgTuples) {
			try {
				sendMsgTuple.send();
			} catch (Exception e) {
				LOG.error("Exception ", e);
			}
		}
	}
}
