package com.example.hidepassword.util;

import java.util.List;

/** Render-only masking logic used by the EditBox mixin. */
public final class PasswordMasker {
	private static final List<CommandMaskRule> COMMAND_PREFIXES =
			List.of(
					new CommandMaskRule("/login", 1),
					new CommandMaskRule("/l", 1),
					new CommandMaskRule("/register", 2),
					new CommandMaskRule("/reg", 2),
					new CommandMaskRule("/account unregister", 1),
					new CommandMaskRule("/account changepassword", 1),
					new CommandMaskRule("/account online", 2),
					new CommandMaskRule("/auth setGlobalPassword", 1),
					new CommandMaskRule("/autologin set", 1));
	private static final String FIXED_MASK = "********";

	private PasswordMasker() {}

	/**
	 * Masks only the render segment supplied by EditBox.TextFormatter.
	 *
	 * @param value the complete, real EditBox value
	 * @param text the render segment supplied by Minecraft
	 * @param offset the segment's UTF-16 offset in {@code value}
	 * @param hideLength whether every masked argument should render as its own fixed
	 *	 eight-character mask
	 * @return the masked render segment, or {@code null} when this segment does not need masking
	 */
	public static String maskRenderSegment(
			String value, String text, int offset, boolean hideLength) {
		if (value == null || text == null || offset < 0 || offset > value.length()) {
			return null;
		}

		long segmentEndLong = (long) offset + text.length();
		if (segmentEndLong > value.length()) {
			return null;
		}

		int segmentEnd = (int) segmentEndLong;
		if (!value.regionMatches(offset, text, 0, text.length())) {
			return null;
		}

		CommandMaskRule rule = findRule(value);
		if (rule == null) {
			return null;
		}

		MaskedValue maskedValue = maskValue(value, rule, hideLength);
		int maskedStart = maskedValue.outputBoundary(offset);
		int maskedEnd = maskedValue.outputBoundary(segmentEnd);
		String maskedSegment = maskedValue.text().substring(maskedStart, maskedEnd);
		return maskedSegment.equals(text) ? null : maskedSegment;
	}

	private static CommandMaskRule findRule(String input) {
		if (input.isEmpty()) {
			return null;
		}

		for (CommandMaskRule rule : COMMAND_PREFIXES) {
			String prefix = rule.prefix();
			int prefixLength = prefix.length();
			if (input.length() > prefixLength
					&& input.regionMatches(true, 0, prefix, 0, prefixLength)
					&& input.charAt(prefixLength) == ' ') {
				return rule;
			}
		}
		return null;
	}

	private static MaskedValue maskValue(String value, CommandMaskRule rule, boolean hideLength) {
		StringBuilder output = new StringBuilder(value.length());
		int[] outputBoundaries = new int[value.length() + 1];
		int argumentStart = rule.prefix().length() + 1;

		copyRange(value, 0, argumentStart, output, outputBoundaries);

		int sourceIndex = argumentStart;
		int argumentIndex = 0;
		while (sourceIndex < value.length()) {
			if (Character.isWhitespace(value.charAt(sourceIndex))) {
				copyCharacter(value, sourceIndex, output, outputBoundaries);
				sourceIndex++;
				continue;
			}

			int argumentEnd = sourceIndex + 1;
			while (argumentEnd < value.length()
					&& !Character.isWhitespace(value.charAt(argumentEnd))) {
				argumentEnd++;
			}

			if (argumentIndex < rule.hiddenArgumentCount()) {
				if (hideLength) {
					appendFixedMask(sourceIndex, argumentEnd, output, outputBoundaries);
				} else {
					appendLengthPreservingMask(sourceIndex, argumentEnd, output, outputBoundaries);
				}
			} else {
				copyRange(value, sourceIndex, argumentEnd, output, outputBoundaries);
			}

			argumentIndex++;
			sourceIndex = argumentEnd;
		}

		return new MaskedValue(output.toString(), outputBoundaries);
	}

	private static void copyRange(
			String value, int start, int end, StringBuilder output, int[] outputBoundaries) {
		for (int index = start; index < end; index++) {
			copyCharacter(value, index, output, outputBoundaries);
		}
	}

	private static void copyCharacter(
			String value, int index, StringBuilder output, int[] outputBoundaries) {
		output.append(value.charAt(index));
		outputBoundaries[index + 1] = output.length();
	}

	private static void appendLengthPreservingMask(
			int start, int end, StringBuilder output, int[] outputBoundaries) {
		for (int index = start; index < end; index++) {
			output.append('*');
			outputBoundaries[index + 1] = output.length();
		}
	}

	private static void appendFixedMask(
			int start, int end, StringBuilder output, int[] outputBoundaries) {
		int argumentLength = end - start;
		int outputStart = output.length();
		output.append(FIXED_MASK);

		for (int sourceOffset = 1; sourceOffset <= argumentLength; sourceOffset++) {
			outputBoundaries[start + sourceOffset] =
					outputStart
							+ (int) ((long) sourceOffset * FIXED_MASK.length() / argumentLength);
		}
	}

	private record CommandMaskRule(String prefix, int hiddenArgumentCount) {
		private CommandMaskRule {
			if (prefix == null || prefix.isEmpty()) {
				throw new IllegalArgumentException("prefix must not be empty");
			}
			if (hiddenArgumentCount < 1) {
				throw new IllegalArgumentException("hiddenArgumentCount must be at least 1");
			}
		}
	}

	private record MaskedValue(String text, int[] outputBoundaries) {
		private int outputBoundary(int sourceOffset) {
			return outputBoundaries[sourceOffset];
		}
	}
}
