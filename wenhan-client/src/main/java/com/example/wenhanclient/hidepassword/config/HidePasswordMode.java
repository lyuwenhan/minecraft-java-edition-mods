package com.example.wenhanclient.hidepassword.config;

public enum HidePasswordMode {
	DISABLED,
	CONTENT_ONLY,
	HIDE_LENGTH;

	public static HidePasswordMode fromLegacy(boolean enabled, boolean hideLength) {
		if (!enabled) {
			return DISABLED;
		}
		return hideLength ? HIDE_LENGTH : CONTENT_ONLY;
	}
}
