package com.example.autogreeting.rules;

import java.util.ArrayList;
import java.util.List;

public class StringMatchRules {
	public final List<String> equal = new ArrayList<>();
	public final List<String> contain = new ArrayList<>();
	public final List<String> startWith = new ArrayList<>();
	public final List<String> endWith = new ArrayList<>();

	public boolean isEmpty() {
		return equal.isEmpty() && contain.isEmpty() && startWith.isEmpty() && endWith.isEmpty();
	}

	public void clear() {
		equal.clear();
		contain.clear();
		startWith.clear();
		endWith.clear();
	}

	public boolean match(String name) {
		for (String v : equal) {
			if (name.equals(v)) {
				return true;
			}
		}

		for (String v : contain) {
			if (name.contains(v)) {
				return true;
			}
		}

		for (String v : startWith) {
			if (name.startsWith(v)) {
				return true;
			}
		}

		for (String v : endWith) {
			if (name.endsWith(v)) {
				return true;
			}
		}

		return false;
	}
}