package com.example.wenhanclient.hidepassword.config;

public class HidePasswordConfig {
	public static final boolean DEFAULT_ENABLED = true;
	public static final boolean DEFAULT_HIDE_LENGTH = false;

	public boolean enabled = DEFAULT_ENABLED;
	public boolean hideLength = DEFAULT_HIDE_LENGTH;

	public HidePasswordConfig copy() {
		HidePasswordConfig copy = new HidePasswordConfig();
		copy.enabled = this.enabled;
		copy.hideLength = this.hideLength;
		return copy;
	}
}
