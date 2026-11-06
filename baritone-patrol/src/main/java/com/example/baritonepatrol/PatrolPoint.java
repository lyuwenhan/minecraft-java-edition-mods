package com.example.baritonepatrol;

public record PatrolPoint(int x, int y, int z) {
	@Override
	public String toString() {
		return x + " " + y + " " + z;
	}
}
