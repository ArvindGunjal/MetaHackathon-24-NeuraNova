package com.meta.hackathon.util;

import java.nio.charset.StandardCharsets;

public class ByteUtils {
	public static byte[] toByteArray(String payload) {
		return payload.getBytes(StandardCharsets.UTF_8);
	}

	public static String toUTFString(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}
}