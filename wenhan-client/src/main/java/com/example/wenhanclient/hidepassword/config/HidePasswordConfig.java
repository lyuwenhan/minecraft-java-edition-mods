package com.example.wenhanclient.hidepassword.config;

public class HidePasswordConfig {
	public boolean enabled = true;
	public boolean hideLength = false;

	public HidePasswordConfig copy() {
		HidePasswordConfig copy = new HidePasswordConfig();
		copy.enabled = this.enabled;
		copy.hideLength = this.hideLength;
		return copy;
	}
}
