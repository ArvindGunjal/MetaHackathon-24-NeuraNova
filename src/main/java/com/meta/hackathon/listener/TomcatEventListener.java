package com.meta.hackathon.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebListener
public class TomcatEventListener implements ServletContextListener {

	private static final Logger LOG = LogManager.getLogger(TomcatEventListener.class.getName());

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		LOG.info("---------TOMCAT START EVENT RECEIVED----------------------");

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		LOG.info("---------TOMCAT SHUTDOWN EVENT RECEIVED----------------------");
		LOG.info("Gracefully shutting tomcat...");
		

	}

}