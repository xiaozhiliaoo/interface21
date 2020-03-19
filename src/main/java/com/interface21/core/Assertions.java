
package com.interface21.core;

public abstract class Assertions {

	public static void assertTrue(String explanation, boolean condition) {
		if (!condition) {
			throw new AssertionFailedException(explanation);
		}
	}
	
	
	static class AssertionFailedException extends RuntimeException {
		public AssertionFailedException(String message) {
			super(message);
		}
	}

}
