package com.example.wenhanclient.greet;

import com.example.wenhanclient.greet.rules.StringMatchRules;

import java.util.ArrayList;
import java.util.List;

public class GreetConfig {
	public static final boolean DEFAULT_SELF_ENABLED = false;
	public static final boolean DEFAULT_OTHER_ENABLED = false;

	public boolean selfEnabled = DEFAULT_SELF_ENABLED;
	public List<String> selfGreetings = new ArrayList<>();
	public boolean otherEnabled = DEFAULT_OTHER_ENABLED;
	public List<String> otherGreetings = new ArrayList<>();
	public StringMatchRules otherBlacklist = new StringMatchRules();
	public StringMatchRules otherBlacklistExcept = new StringMatchRules();
	public StringMatchRules otherWhitelist = new StringMatchRules();
	public StringMatchRules otherWhitelistExcept = new StringMatchRules();

	public GreetConfig copy() {
		GreetConfig copy = new GreetConfig();
		copy.selfEnabled = selfEnabled;
		copy.otherEnabled = otherEnabled;
		if (selfGreetings != null) copy.selfGreetings.addAll(selfGreetings);
		if (otherGreetings != null) copy.otherGreetings.addAll(otherGreetings);
		if (otherBlacklist != null) copy.otherBlacklist = otherBlacklist.copy();
		if (otherBlacklistExcept != null) copy.otherBlacklistExcept = otherBlacklistExcept.copy();
		if (otherWhitelist != null) copy.otherWhitelist = otherWhitelist.copy();
		if (otherWhitelistExcept != null) copy.otherWhitelistExcept = otherWhitelistExcept.copy();
		return copy;
	}

	public void fillMissing() {
		if (selfGreetings == null) {
			selfGreetings = new ArrayList<>();
		}
		if (otherGreetings == null) {
			otherGreetings = new ArrayList<>();
		}
		if (otherBlacklist == null) {
			otherBlacklist = new StringMatchRules();
		}
		if (otherBlacklistExcept == null) {
			otherBlacklistExcept = new StringMatchRules();
		}
		if (otherWhitelist == null) {
			otherWhitelist = new StringMatchRules();
		}
		if (otherWhitelistExcept == null) {
			otherWhitelistExcept = new StringMatchRules();
		}
		otherBlacklist.fillMissing();
		otherBlacklistExcept.fillMissing();
		otherWhitelist.fillMissing();
		otherWhitelistExcept.fillMissing();
	}
}
