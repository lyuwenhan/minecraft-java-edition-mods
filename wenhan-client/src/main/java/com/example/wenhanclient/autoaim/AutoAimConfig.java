package com.example.wenhanclient.autoaim;

public final class AutoAimConfig {
	public static final AutoAimMode DEFAULT_MODE = AutoAimMode.DISABLED;
	public static final ViewRestoreMode DEFAULT_VIEW_RESTORE = ViewRestoreMode.DISABLED;
	public static final double DEFAULT_MAX_ATTACK_DISTANCE = 6.0D;
	public static final double MIN_MAX_ATTACK_DISTANCE = 0.0D;
	public static final double MAX_MAX_ATTACK_DISTANCE = Double.MAX_VALUE;
	public static final boolean DEFAULT_ATTACK_THROUGH_WALLS = false;
	public static final boolean DEFAULT_HIGHLIGHT_TARGET = false;
	public static final int DEFAULT_HIGHLIGHT_COLOR = 0xFF5555;
	public static final AutoAttackMode DEFAULT_AUTO_ATTACK = AutoAttackMode.DISABLED;
	public static final int DEFAULT_AUTO_ATTACK_DELAY_TICKS = 0;
	public static final int MAX_AUTO_ATTACK_DELAY_TICKS = 1200;

	public AutoAimMode mode = DEFAULT_MODE;
	public ViewRestoreMode viewRestore = DEFAULT_VIEW_RESTORE;
	public double maxAttackDistance = DEFAULT_MAX_ATTACK_DISTANCE;
	public boolean attackThroughWalls = DEFAULT_ATTACK_THROUGH_WALLS;
	public boolean highlightTarget = DEFAULT_HIGHLIGHT_TARGET;
	public int highlightColor = DEFAULT_HIGHLIGHT_COLOR;
	public AutoAttackMode autoAttack = DEFAULT_AUTO_ATTACK;
	public int autoAttackDelayTicks = DEFAULT_AUTO_ATTACK_DELAY_TICKS;

	public void sanitize() {
		if (mode == null) {
			mode = DEFAULT_MODE;
		}
		if (autoAttack == null) {
			autoAttack = DEFAULT_AUTO_ATTACK;
		}
		if (viewRestore == null) {
			viewRestore = DEFAULT_VIEW_RESTORE;
		}
		if (!Double.isFinite(maxAttackDistance)) {
			maxAttackDistance = DEFAULT_MAX_ATTACK_DISTANCE;
		} else {
			maxAttackDistance =
					Math.max(
							MIN_MAX_ATTACK_DISTANCE,
							Math.min(MAX_MAX_ATTACK_DISTANCE, maxAttackDistance));
		}
		highlightColor &= 0xFFFFFF;
		autoAttackDelayTicks =
				Math.max(0, Math.min(MAX_AUTO_ATTACK_DELAY_TICKS, autoAttackDelayTicks));
	}

	public AutoAimConfig copy() {
		AutoAimConfig copy = new AutoAimConfig();
		copy.mode = this.mode;
		copy.viewRestore = this.viewRestore;
		copy.maxAttackDistance = this.maxAttackDistance;
		copy.attackThroughWalls = this.attackThroughWalls;
		copy.highlightTarget = this.highlightTarget;
		copy.highlightColor = this.highlightColor;
		copy.autoAttack = this.autoAttack;
		copy.autoAttackDelayTicks = this.autoAttackDelayTicks;
		copy.sanitize();
		return copy;
	}
}
