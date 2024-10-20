package com.meta.hackathon.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReloadableCache {
	private static final Logger LOG = LogManager.getLogger(ReloadableCache.class.getName());
	public static final ReloadableCache instance = new ReloadableCache();
	private Map<String, String> allPropertiesMap;
	private RefreshThread refThread;
	private volatile boolean isRunning = true;

	private ReloadableCache() {
		reloadConfig();
		refThread = new RefreshThread();
		refThread.start();
	}

	public String get(String propertyKey) {
		String propertyValue = "";
		try {
			propertyValue = allPropertiesMap.get(propertyKey);
		} catch (Exception e) {
			LOG.error("Exception while getting property value ", e);
			propertyValue = "";
		}
		return propertyValue;
	}

	private void reloadConfig() {
		LOG.info("Caching all properties from file...");
		if (allPropertiesMap == null)
			allPropertiesMap = new HashMap<>();

		Map<String, String> reloadedPropertiesMap = new HashMap<>();
		try {
			Properties allProperties = new PropertyLoader().loadProps("config.properties");
			Set<Object> allPropertyKeys = allProperties.keySet();
			for (Object propertyKey : allPropertyKeys) {
				reloadedPropertiesMap.put(propertyKey.toString(), allProperties.getProperty(propertyKey.toString()));
			}
			allPropertiesMap = reloadedPropertiesMap;
			LOG.info("All properties cached successfully, no of properties found: {}", allProperties.size());
		} catch (Exception e) {
			LOG.error("Unable to load props: ", e);
		}
	}

	private class RefreshThread extends Thread {
		@Override
		public void run() {
			while (isRunning) {
				reloadConfig();
				try {
					sleep(1800 * 1000L); // Every 30 Mins
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					LOG.error("Exception occured during sleep: ", e);
				}
			}
		}
	}

	public void reload() {
		if (!isRunning) {
			isRunning = true;
			refThread = new RefreshThread();
			refThread.start();
			LOG.info("ReloadableCache started");
		} else {
			reloadConfig();
			LOG.info("reloadConfig() function called");
		}
	}
}
