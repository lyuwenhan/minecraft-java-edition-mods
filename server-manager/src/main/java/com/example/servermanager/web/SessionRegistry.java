package com.example.servermanager.web;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.AttributeKey;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionRegistry {
	public static final String COOKIE_NAME = "SMSESSION";
	public static final long COOKIE_MAX_AGE_SECONDS = Duration.ofDays(1).toSeconds();
	public static final AttributeKey<String> SESSION_ID =
			AttributeKey.valueOf("serverManagerSessionId");

	private static final Duration INACTIVITY_TIMEOUT = Duration.ofHours(1);
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<>();

	private SessionRegistry() {}

	public static Session rotateAuthenticated(FullHttpRequest request, String username) {
		cleanupExpired();
		String oldId = readCookie(request);
		if (oldId != null) invalidateSession(oldId);
		Session session = createSession();
		session.authenticated = true;
		session.username = username;
		session.touch();
		return session;
	}

	public static Session resolve(FullHttpRequest request) {
		cleanupExpired();
		String id = readCookie(request);
		if (id == null) return null;
		Session session = SESSIONS.get(id);
		if (session == null || isExpired(session, Instant.now())) {
			if (session != null) invalidateSession(id);
			return null;
		}
		session.touch();
		return session;
	}

	public static boolean isAuthenticated(Session session) {
		if (session == null || !session.authenticated) return false;
		if (isExpired(session, Instant.now())) {
			invalidateSession(session.id());
			return false;
		}
		return true;
	}

	public static void attach(Channel channel, Session session) {
		if (!isAuthenticated(session)) return;
		channel.attr(SESSION_ID).set(session.id());
		session.channels.add(channel);
		session.touch();
	}

	public static Session forChannel(Channel channel) {
		cleanupExpired();
		String id = channel.attr(SESSION_ID).get();
		if (id == null) return null;
		Session session = SESSIONS.get(id);
		if (!isAuthenticated(session)) return null;
		return session;
	}

	public static void detach(Channel channel) {
		String id = channel.attr(SESSION_ID).getAndSet(null);
		if (id == null) return;
		Session session = SESSIONS.get(id);
		if (session != null) session.channels.remove(channel);
	}

	public static void invalidateUsername(String username) {
		if (username == null) return;
		for (Session session : SESSIONS.values()) {
			if (session.authenticated && username.equalsIgnoreCase(session.username)) {
				invalidateSession(session.id());
			}
		}
	}

	public static void invalidateAll() {
		for (String id : Set.copyOf(SESSIONS.keySet())) invalidateSession(id);
	}

	public static Set<Channel> authenticatedChannels() {
		cleanupExpired();
		Set<Channel> result = ConcurrentHashMap.newKeySet();
		for (Session session : SESSIONS.values()) {
			if (isAuthenticated(session)) result.addAll(session.channels);
		}
		return result;
	}

	private static Session createSession() {
		Session created;
		do {
			created = new Session(generateSessionId());
		} while (SESSIONS.putIfAbsent(created.id(), created) != null);
		return created;
	}

	private static void cleanupExpired() {
		Instant now = Instant.now();
		for (Session session : SESSIONS.values()) {
			if (isExpired(session, now)) invalidateSession(session.id());
		}
	}

	private static boolean isExpired(Session session, Instant now) {
		return Duration.between(session.lastAccessedAt(), now).compareTo(INACTIVITY_TIMEOUT) >= 0;
	}

	private static void invalidateSession(String id) {
		Session session = SESSIONS.remove(id);
		if (session == null) return;
		session.authenticated = false;
		for (Channel channel : session.channels) {
			channel.attr(SESSION_ID).compareAndSet(id, null);
			channel.close();
		}
		session.channels.clear();
	}

	private static String readCookie(FullHttpRequest request) {
		String header = request.headers().get(HttpHeaderNames.COOKIE);
		if (header == null || header.isBlank()) return null;
		for (Cookie cookie : ServerCookieDecoder.STRICT.decodeAll(header)) {
			if (COOKIE_NAME.equals(cookie.name()) && isValidSessionId(cookie.value()))
				return cookie.value();
		}
		return null;
	}

	private static boolean isValidSessionId(String value) {
		if (value == null || value.length() != 43) return false;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (!(c >= 'a' && c <= 'z'
					|| c >= 'A' && c <= 'Z'
					|| c >= '0' && c <= '9'
					|| c == '-'
					|| c == '_')) return false;
		}
		return true;
	}

	private static String generateSessionId() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public static final class Session {
		private final String id;
		private final Instant createdAt = Instant.now();
		private final Set<Channel> channels = ConcurrentHashMap.newKeySet();
		private volatile Instant lastAccessedAt = createdAt;
		private volatile boolean authenticated;
		private volatile String username;

		private Session(String id) {
			this.id = id;
		}

		public String id() {
			return id;
		}

		public Instant createdAt() {
			return createdAt;
		}

		public Instant lastAccessedAt() {
			return lastAccessedAt;
		}

		public boolean authenticated() {
			return authenticated;
		}

		public String username() {
			return username;
		}

		private void touch() {
			lastAccessedAt = Instant.now();
		}
	}
}
