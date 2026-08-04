package com.example.servermanager.web;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

final class LoginRateLimiter {
	private static final Duration SHORT_WINDOW = Duration.ofMinutes(5);
	private static final Duration LONG_WINDOW = Duration.ofHours(1);
	private static final int SHORT_LIMIT = 10;
	private static final int LONG_LIMIT = 200;
	private static final ConcurrentHashMap<String, FailureHistory> FAILURES =
			new ConcurrentHashMap<>();

	private LoginRateLimiter() {}

	static boolean isBlocked(String ip) {
		FailureHistory history = FAILURES.get(ip);
		if (history == null) return false;
		return history.isBlocked(Instant.now());
	}

	static void recordFailure(String ip) {
		FAILURES.computeIfAbsent(ip, ignored -> new FailureHistory()).record(Instant.now());
	}

	static void recordSuccess(String ip) {
		FAILURES.remove(ip);
	}

	private static final class FailureHistory {
		private final Deque<Instant> failures = new ArrayDeque<>();

		synchronized void record(Instant now) {
			prune(now);
			failures.addLast(now);
		}

		synchronized boolean isBlocked(Instant now) {
			prune(now);
			int inShortWindow = 0;
			Instant shortCutoff = now.minus(SHORT_WINDOW);
			for (Instant failure : failures) if (!failure.isBefore(shortCutoff)) inShortWindow++;
			return inShortWindow >= SHORT_LIMIT || failures.size() >= LONG_LIMIT;
		}

		private void prune(Instant now) {
			Instant cutoff = now.minus(LONG_WINDOW);
			while (!failures.isEmpty() && failures.peekFirst().isBefore(cutoff))
				failures.removeFirst();
		}
	}
}
