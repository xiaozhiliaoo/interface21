
package com.interface21.load;

import java.util.Random;

public class TestTestSuite extends AbstractTestSuite {

	/**
	 * @see AbstractTestSuite#createTests()
	 */
	protected Test[] createTests() {
		Test[] tests = new Test[getThreads()];
		for (int i = 0; i < tests.length; i++)
			tests[i] = new TestTest();
		return tests;
	}
	 
	
	public static void main(String[] args) {
		TestTestSuite tests = new TestTestSuite();
		tests.setThreads(7);
		tests.setPasses(10);
		tests.setMaxPause(123L);
		tests.setReportInterval(1000L);
		tests.runAllTests(true);
	}
	
	
	public static class TestTest extends AbstractTest {
		Random rand ;
		
		public TestTest() {
			rand = new Random(hashCode());
		}
		
		/**
		 * @see AbstractTest#runPass(int)
		 */
		protected void runPass(int i) throws TestFailedException {
			try {
				//System.out.println(getName() + " run pass " + i);
				Thread.sleep(rand.nextInt(5000));
			}
			catch (InterruptedException ex) {
			}
		}

	}

}
