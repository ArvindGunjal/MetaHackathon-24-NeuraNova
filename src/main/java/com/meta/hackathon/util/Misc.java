package com.meta.hackathon.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

public class Misc {

	public static int generateRandomNumber(int start, int end) {
		if (start > end) {
			throw new IllegalArgumentException("Start must be less than or equal to end.");
		}
		Random random = new Random();
		return random.nextInt((end - start) + 1) + start;
	}

	public static String getCurrentTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		return now.format(formatter);
	}

	public static boolean isTimeToExecute(String cronExpression) {
		// Create a parser for Unix cron expressions
		CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
		Cron cron = parser.parse(cronExpression);
		cron.validate(); // Validate the cron expression
		ZonedDateTime now = ZonedDateTime.now();
		// Check if the cron expression matches the current time
		ExecutionTime executionTime = ExecutionTime.forCron(cron);
		Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
		Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(now);
		if (lastExecution.isPresent() && lastExecution.get().isBefore(now) && nextExecution.isPresent()
				&& nextExecution.get().isAfter(now)) {
			return true;
		} else {
			return false;
		}
	}

	public static void downloadFile(String fileURL, String destination) throws IOException {
		URL url = new URL(fileURL);
		// Open a readable channel from the URL
		try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
				FileOutputStream fileOutputStream = new FileOutputStream(destination)) {
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
		}
	}

	public static void convertBase64EncodedToWavFile(String base64EncodedString, String outputFilePath)
			throws IOException {
		byte[] audioBytes = Base64.getDecoder().decode(base64EncodedString);
		File outputFile = new File(outputFilePath);
		try (FileOutputStream fos = new FileOutputStream(outputFile)) {
			fos.write(audioBytes);
		}
	}

	public static void main(String[] args) {
		// Define the cron expression: 59 18 17 * * (17th of every month at 6:59 PM)
		String cronExpression = "59 18 17 * *";

		// Create a parser for Unix cron expressions
		CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
		Cron cron = parser.parse(cronExpression);
		cron.validate(); // Validate the cron expression

		// Get the current time
		ZonedDateTime now = ZonedDateTime.now();

		// Check if the cron expression matches the current time
		ExecutionTime executionTime = ExecutionTime.forCron(cron);
		Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
		Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(now);

		if (lastExecution.isPresent() && lastExecution.get().isBefore(now) && nextExecution.isPresent()
				&& nextExecution.get().isAfter(now)) {
			System.out.println("It's time to execute the task!");
		} else {
			System.out.println("Not the right time yet.");
		}
	}

}
