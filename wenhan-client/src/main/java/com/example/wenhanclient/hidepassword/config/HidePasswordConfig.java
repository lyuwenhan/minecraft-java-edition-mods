package com.example.wenhanclient.hidepassword.config;

public class HidePasswordConfig {
	public static final boolean DEFAULT_ENABLED = true;
	public static final boolean DEFAULT_HIDE_LENGTH = false;
	public static final HidePasswordMode DEFAULT_MODE =
			HidePasswordMode.fromLegacy(DEFAULT_ENABLED, DEFAULT_HIDE_LENGTH);

	public boolean enabled = DEFAULT_ENABLED;
	public boolean hideLength = DEFAULT_HIDE_LENGTH;

	public HidePasswordMode mode() {
		return HidePasswordMode.fromLegacy(enabled, hideLength);
	}

	public void setMode(HidePasswordMode mode) {
		HidePasswordMode resolved = mode == null ? DEFAULT_MODE : mode;
		this.enabled = resolved != HidePasswordMode.DISABLED;
		if (this.enabled) {
			this.hideLength = resolved == HidePasswordMode.HIDE_LENGTH;
		}
	}

	public HidePasswordConfig copy() {
		HidePasswordConfig copy = new HidePasswordConfig();
		copy.enabled = this.enabled;
		copy.hideLength = this.hideLength;
		return copy;
	}
}
