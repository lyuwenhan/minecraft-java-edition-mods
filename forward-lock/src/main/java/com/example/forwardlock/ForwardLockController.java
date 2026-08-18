package com.example.forwardlock;

/** Pure input state machine for forward-lock activation and cancellation. */
final class ForwardLockController {
	static final int DOUBLE_TAP_WINDOW_TICKS = 7;

	private boolean locked;
	private boolean previousForwardDown;
	private boolean previousBackwardDown;
	private boolean previousCrouchDown;
	private long tick;
	private long firstForwardPressTick = Long.MIN_VALUE;

	void update(
			boolean forwardDown,
			boolean backwardDown,
			boolean crouchDown,
			boolean sprintDown,
			boolean processPresses) {
		boolean forwardPressed = forwardDown && !previousForwardDown;
		boolean backwardPressed = backwardDown && !previousBackwardDown;
		boolean crouchPressed = crouchDown && !previousCrouchDown;

		previousForwardDown = forwardDown;
		previousBackwardDown = backwardDown;
		previousCrouchDown = crouchDown;

		if (!processPresses) {
			firstForwardPressTick = Long.MIN_VALUE;
			tick++;
			return;
		}

		if (locked) {
			if (forwardPressed || backwardPressed || crouchPressed) {
				locked = false;
				firstForwardPressTick = Long.MIN_VALUE;
			}
			tick++;
			return;
		}

		if (backwardPressed || crouchPressed) {
			firstForwardPressTick = Long.MIN_VALUE;
		} else if (forwardPressed) {
			if (sprintDown
					&& firstForwardPressTick != Long.MIN_VALUE
					&& tick - firstForwardPressTick <= DOUBLE_TAP_WINDOW_TICKS) {
				locked = true;
				firstForwardPressTick = Long.MIN_VALUE;
			} else {
				firstForwardPressTick = tick;
			}
		}

		tick++;
	}

	boolean isLocked() {
		return locked;
	}

	void reset() {
		locked = false;
		previousForwardDown = false;
		previousBackwardDown = false;
		previousCrouchDown = false;
		tick = 0L;
		firstForwardPressTick = Long.MIN_VALUE;
	}
}
