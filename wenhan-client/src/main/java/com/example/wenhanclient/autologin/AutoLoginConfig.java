package com.example.wenhanclient.autologin;

import com.example.wenhanclient.WenhanClientMod;

import java.util.HashMap;
import java.util.Map;

public class AutoLoginConfig {
	public Map<String, Credential> servers = new HashMap<>();

	public static class Credential {
		public static final boolean DEFAULT_ENABLED = true;

		public String enc;
		public String salt;
		public String iv;
		public boolean enabled = DEFAULT_ENABLED;

		public boolean hasPassword() {
			return enc != null && !enc.isEmpty() && iv != null && !iv.isEmpty();
		}

		public void clearPassword() {
			enc = null;
			salt = null;
			iv = null;
		}

		public Credential copy() {
			Credential copy = new Credential();
			copy.enc = enc;
			copy.salt = salt;
			copy.iv = iv;
			copy.enabled = enabled;
			return copy;
		}
	}

	public AutoLoginConfig copy() {
		AutoLoginConfig copy = new AutoLoginConfig();
		if (servers != null) {
			servers.forEach(
					(address, credential) -> {
						if (address != null && !address.isBlank() && credential != null) {
							copy.servers.put(address, credential.copy());
						}
					});
		}
		return copy;
	}

	public static AutoLoginConfig load() {
		AutoLoginConfig cfg = WenhanClientMod.CONFIG.autoLogin;
		if (cfg == null) {
			cfg = new AutoLoginConfig();
			WenhanClientMod.CONFIG.autoLogin = cfg;
		}
		if (cfg.servers == null) {
			cfg.servers = new HashMap<>();
		}
		return cfg;
	}

	public void save() {
		if (this.servers == null) {
			this.servers = new HashMap<>();
		}
		WenhanClientMod.CONFIG.autoLogin = this;
		WenhanClientMod.CONFIG.save();
	}
}
