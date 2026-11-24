package com.example.wenhanclient.freecaminteraction;

public final class FreecamInteractionConfig {
	public boolean enabled = false;
	public boolean restoreView = true;

	public FreecamInteractionMode mode() {
		return !enabled
				? FreecamInteractionMode.DISABLED
				: restoreView ? FreecamInteractionMode.RESTORE : FreecamInteractionMode.ENABLED;
	}

	public void setMode(FreecamInteractionMode mode) {
		enabled = mode != null && mode != FreecamInteractionMode.DISABLED;
		restoreView = mode != FreecamInteractionMode.ENABLED;
	}

	public FreecamInteractionConfig copy() {
		FreecamInteractionConfig copy = new FreecamInteractionConfig();
		copy.enabled = enabled;
		copy.restoreView = restoreView;
		return copy;
	}
}
