package com.example.wenhanclient.greet;

import com.example.wenhanclient.greet.rules.StringMatchRules;

import java.util.ArrayList;
import java.util.List;

public class GreetConfig {
	public boolean selfEnabled = true;
	public List<String> selfGreetings = new ArrayList<>();
	public boolean otherEnabled = true;
	public List<String> otherGreetings = new ArrayList<>();
	public StringMatchRules otherBlacklist = new StringMatchRules();
	public StringMatchRules otherBlacklistExcept = new StringMatchRules();
	public StringMatchRules otherWhitelist = new StringMatchRules();
	public StringMatchRules otherWhitelistExcept = new StringMatchRules();

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
