package com.example.wenhanclient.jumpheight;

public final class JumpHeightCalculator {
	private static final double MINIMUM_STRENGTH = 0.003D;
	private static final double HEIGHT_OFFSET = 0.1D;

	private JumpHeightCalculator() {}

	public static double estimateStrength(double height, double gravity) {
		return Math.sqrt(2.0D * gravity * height);
	}

	public static int calculateTicks(double strength, double gravity) {
		if (strength <= MINIMUM_STRENGTH) {
			return 0;
		}

		return Math.max(
				0,
				(int)
								Math.floor(
										Math.log(
														(strength + 49.0D * gravity)
																/ (49.0D * gravity
																		+ MINIMUM_STRENGTH))
												/ Math.log(50.0D / 49.0D))
						+ 1);
	}

	public static double calculateHeight(double strength, double gravity) {
		int ticks = calculateTicks(strength, gravity);
		return 50.0D * (strength + 49.0D * gravity) * (1.0D - Math.pow(0.98D, ticks))
				- 49.0D * gravity * ticks;
	}

	public static double refineStrength(double strength, double height, double gravity) {
		int ticks = calculateTicks(strength, gravity);
		if (ticks == 0) {
			return Math.max(MINIMUM_STRENGTH, strength);
		}

		double currentHeight = calculateHeight(strength, gravity);
		double derivative = 50.0D * (1.0D - Math.pow(0.98D, ticks));
		return Math.max(0.0D, strength + (height - currentHeight) / derivative);
	}

	public static double inverseHeight(double height, double gravity) {
		height += HEIGHT_OFFSET;
		if (!Double.isFinite(height)
				|| !Double.isFinite(gravity)
				|| height < 0.0D
				|| gravity <= 0.0D
				|| gravity > 1.0D) {
			throw new IllegalArgumentException("Invalid height or gravity");
		}

		if (height == 0.0D) {
			return 0.0D;
		}

		double strength = estimateStrength(height, gravity);
		for (int iteration = 0; iteration < 20; iteration++) {
			double nextStrength = refineStrength(strength, height, gravity);
			if (Math.abs(calculateHeight(nextStrength, gravity) - height) < 0.0005D) {
				return nextStrength;
			}
			if (Math.abs(nextStrength - strength) < 1.0E-12D) {
				break;
			}
			strength = nextStrength;
		}

		return strength;
	}
}
