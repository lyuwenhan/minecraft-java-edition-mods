package com.example.wenhanclient.transparentblocks;

import com.example.wenhanclient.WenhanClientMod;

public final class TransparentBlocksConfig {
	public boolean alwaysRender = false;

	public static TransparentBlocksConfig load() {
		return WenhanClientMod.CONFIG.transparentBlocks.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.transparentBlocks = copy();
		WenhanClientMod.CONFIG.save();
	}

	public TransparentBlocksConfig copy() {
		TransparentBlocksConfig copy = new TransparentBlocksConfig();
		copy.alwaysRender = this.alwaysRender;
		return copy;
	}
}
