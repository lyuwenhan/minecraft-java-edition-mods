package com.example.servermanager.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

public final class LogBuffer {
	private static final int MAX = 1_000_000;
	private static final StringBuilder BUFFER = new StringBuilder();
	private static AbstractAppender appender;

	private LogBuffer() {}

	public static synchronized void install() {
		if (appender != null) return;
		PatternLayout layout =
				PatternLayout.newBuilder()
						.withPattern("[%d{HH:mm:ss}] [%t/%level]: %msg%n")
						.build();
		appender =
				new AbstractAppender("ServerManagerWeb", null, layout, false, null) {
					@Override
					public void append(LogEvent event) {
						String text = new String(getLayout().toByteArray(event));
						synchronized (BUFFER) {
							BUFFER.append(text);
							if (BUFFER.length() > MAX) BUFFER.delete(0, BUFFER.length() - MAX);
						}
						BackendService.broadcast(
								"{\"type\":\"append-log\",\"content\":" + json(text) + "}");
					}
				};
		appender.start();
		((Logger) LogManager.getRootLogger()).addAppender(appender);
	}

	public static String fullLogPacket() {
		synchronized (BUFFER) {
			return "{\"type\":\"full-log\",\"content\":" + json(BUFFER.toString()) + "}";
		}
	}

	private static String json(String s) {
		return new com.google.gson.Gson().toJson(s);
	}
}
