package com.example.wenhanclient.autologin;

import com.example.wenhanclient.WenhanClientMod;
import java.util.HashMap;
import java.util.Map;

public class AutoLoginConfig {
	public Map<String, Credential> servers = new HashMap<>();

	public static class Credential {
		public String enc;
		public String salt;
		public String iv;
		public boolean enabled = true;
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
