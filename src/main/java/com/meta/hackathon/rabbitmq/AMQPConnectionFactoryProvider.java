package com.meta.hackathon.rabbitmq;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.ForgivingExceptionHandler;

public class AMQPConnectionFactoryProvider {
	public static ConnectionFactory getRabbitMQConnectionFactory() {
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setConnectionTimeout(30_000);
		connectionFactory.setHandshakeTimeout(30_000);
		connectionFactory.setNetworkRecoveryInterval(100);
		connectionFactory.setExceptionHandler(new ForgivingExceptionHandler());
		connectionFactory.setAutomaticRecoveryEnabled(true);
		try {
			// if(CommandLineOptions.RMQ_ENABLE_TLS.obtainValueFromCLI(cli).equals(true)) {
			connectionFactory.useSslProtocol();
			// }
			return connectionFactory;
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
}