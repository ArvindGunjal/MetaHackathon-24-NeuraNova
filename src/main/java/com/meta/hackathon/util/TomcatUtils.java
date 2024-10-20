package com.meta.hackathon.util;

import java.io.File;

public class TomcatUtils {

	private TomcatUtils() {

	}

	public static String getTomcatName() {
		String tomcatPath = System.getProperty("catalina.home");
		if (!tomcatPath.equals("")) {
			String tmp[] = tomcatPath.split(File.separator);
			if (tmp.length > 1)
				return tmp[tmp.length - 1];
		}
		return "";
	}

}