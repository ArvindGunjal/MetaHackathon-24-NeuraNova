package com.meta.hackathon.api.service.config.impl;

import com.meta.hackathon.api.service.config.ConfigService;
import com.meta.hackathon.config.ReloadableCache;

public class ConfigServiceImpl implements ConfigService {

	public static final ConfigServiceImpl instance = new ConfigServiceImpl();

	@Override
	public void reload(String cacheName) throws Exception {
		if (cacheName.equalsIgnoreCase("reloadablecache")) {
			ReloadableCache.instance.reload();
		}
	}
}
