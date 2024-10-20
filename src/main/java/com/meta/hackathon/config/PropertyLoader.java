package com.meta.hackathon.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertyLoader {
	private static final Logger APPLOG = LogManager.getLogger(PropertyLoader.class.getName());

	public Properties loadProps(String path) {
		Properties allProperties = new Properties();
		try (InputStream in = this.getClass().getResourceAsStream("/" + path)) {
			allProperties.load(in);
		} catch (IOException ex) {
			APPLOG.error("Error in reading file {}", path, ex);
		}
		return allProperties;
	}
}