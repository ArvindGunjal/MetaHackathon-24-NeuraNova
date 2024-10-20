package com.meta.hackathon.queue;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.meta.hackathon.rabbitmq.AMQPConnectionFactoryProvider;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ProcessorRabbitMQ {

	public static final ProcessorRabbitMQ instance = new ProcessorRabbitMQ();
	private static final Logger LOG = LogManager.getLogger(ProcessorRabbitMQ.class.getSimpleName());
	private static final String PROCESSOR_QUEUE = "processor_queue";
	private static ExecutorService executor;
	private static Channel channel;
	private static Connection connection;

	private ProcessorRabbitMQ() {
		channel = createAMQPChannel();
		executor = Executors.newFixedThreadPool(10);
		if (channel == null) {
			LOG.error("Cannot Obtain RMQ Channel, Exiting Application");
		}
		boolean success = declareQueue(channel);
		if (!success) {
			LOG.error("Unable to declare desired queue, Exiting Application");
		}
	}

	public void publishMessage(String message) {
		executor.execute(() -> {
			try {
				if (channel != null && channel.isOpen()) {
					channel.basicPublish("", PROCESSOR_QUEUE, null, message.getBytes());
					LOG.info("Message sent to {} : {}", PROCESSOR_QUEUE, message);
				} else {
					LOG.error("Channel for queue " + PROCESSOR_QUEUE + " is not available.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public void close() throws Exception {
		if (channel != null && channel.isOpen()) {
			channel.close();
		}
		if (connection != null && connection.isOpen()) {
			connection.close();
		}
		executor.shutdown();
		LOG.info("RabbitMQ resources closed.");
	}

	private static boolean declareQueue(Channel channel) {
		try {
			channel.queueDeclare(PROCESSOR_QUEUE, true, false, false, new HashMap<>());
			return true;
		} catch (IOException e) {
			LOG.error("Exception while declaring Queue | Reason => ", e);
		}
		return false;
	}

	private static Channel createAMQPChannel() {
		try {
			ConnectionFactory connectionFactory = AMQPConnectionFactoryProvider.getRabbitMQConnectionFactory();
			Address[] addresses = Address.parseAddresses("amqp://guest:guest@localhost:5672/");
			connectionFactory.setUsername("guest");
			connectionFactory.setPassword("guest");
			connection = connectionFactory.newConnection(addresses);
			return connection.createChannel();
		} catch (IOException | TimeoutException e) {
			LOG.error("Exception occurred while Creating AMQP Channel | Reason => ", e);
		}
		return null;
	}

}
