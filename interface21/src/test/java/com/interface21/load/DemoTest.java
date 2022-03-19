
package com.interface21.load;

import java.util.Random;

public class DemoTest extends AbstractTest {
		Random rand;

		public DemoTest() {
			rand = new Random(hashCode());
		}

		/**
		 * @see AbstractTest#runPass(int)
		 */
		protected void runPass(int i) throws TestFailedException {
			try {
				//System.out.println(getName() + " run pass " + i);
				Thread.sleep(rand.nextInt(500));
			}
			catch (InterruptedException ex) {
			}
		}

	}
