package com.example.wenhanclient.clientpseudounrestrict;

import com.example.wenhanclient.WenhanClientMod;

public final class ClientPseudoUnrestrictConfig {
	public static final boolean DEFAULT_BLOCK_INTERACTION_RANGE = false;
	public static final boolean DEFAULT_ENTITY_INTERACTION_RANGE = false;
	public static final boolean DEFAULT_INSTANT_BREAK = false;

	public boolean blockInteractionRange = DEFAULT_BLOCK_INTERACTION_RANGE;
	public boolean entityInteractionRange = DEFAULT_ENTITY_INTERACTION_RANGE;
	public boolean instantBreak = DEFAULT_INSTANT_BREAK;

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
