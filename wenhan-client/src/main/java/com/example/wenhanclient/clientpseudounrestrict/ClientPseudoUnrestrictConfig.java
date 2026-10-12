package com.example.wenhanclient.clientpseudounrestrict;

import com.example.wenhanclient.WenhanClientMod;

public final class ClientPseudoUnrestrictConfig {
	public boolean blockInteractionRange = false;
	public boolean entityInteractionRange = false;
	public boolean instantBreak = false;

	public static ClientPseudoUnrestrictConfig load() {
		return WenhanClientMod.CONFIG.clientPseudoUnrestrict.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.clientPseudoUnrestrict = copy();
		WenhanClientMod.CONFIG.save();
	}

	public ClientPseudoUnrestrictConfig copy() {
		ClientPseudoUnrestrictConfig copy = new ClientPseudoUnrestrictConfig();
		copy.blockInteractionRange = this.blockInteractionRange;
		copy.entityInteractionRange = this.entityInteractionRange;
		copy.instantBreak = this.instantBreak;
		return copy;
	}
}
