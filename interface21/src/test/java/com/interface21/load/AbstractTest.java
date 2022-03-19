package com.interface21.load;

import java.util.*;

import com.interface21.util.ResponseTimeMonitor;
import com.interface21.util.ResponseTimeMonitorImpl;
import com.interface21.util.StopWatch;

/**
 * SHOULD REALLY FIRE EVENTS
 * @author  Rod Johnson
 * @since February 9, 2001
 */
public abstract class AbstractTest implements Test {

	/** Used to calculate delays */
	private static Random rand = new Random();

	private long maxPause = -1L;
	private int nbPasses;

	private StopWatch runningTimer = new StopWatch();
	private StopWatch pauseTimer = new StopWatch();
	private StopWatch elapsedTimer = new StopWatch();

	private List testFailedExceptions;
	
	private boolean longReports;

	private int completedTests;
	
	private int instances = 1;

	
	private String name;

	private ResponseTimeMonitorImpl responseTimeMonitorImpl = new ResponseTimeMonitorImpl();

	private AbstractTestSuite suite;

	protected AbstractTest() {
		testFailedExceptions = new ArrayList(nbPasses);
	}
	
	/**
	 * Subclasses can implement this to save a test
	 * fixture to run application-specific tests against.
	 * @param context application-specific object to test
	 */
	public void setFixture(Object context) {
	}
	
	public void setInstances(int instances) {
		this.instances = instances;
	}
	
	public int getInstances() {
		return instances;
	}

	
	/**
	 * Must be set by framework
	 */
	public void setTestSuite(AbstractTestSuite suite) {
		this.suite = suite;
		// Set defaults if not set
		if (getPasses() == 0)
			this.setPasses(suite.getPasses());
		if (getMaxPause() <= 0L)
			this.setMaxPause(suite.getMaxPause());
		this.setLongReports(suite.getLongReports());
	}


	public final void setName(String name) {
		this.name = name;
	}
	
	public final void setLongReports(boolean longReports) {
		this.longReports = longReports;
	}

	public final void setPasses(int nbPasses) {
		this.nbPasses = nbPasses;
	}

	public final void setMaxPause(long maxPause) {
		this.maxPause = maxPause;
	}
	
	public final long getMaxPause() {
		return this.maxPause;
	}

	public final int getPasses() {
		return nbPasses;
	}

	public final int getErrorCount() {
		return testFailedExceptions.size();
	}

	public final String getName() {
		return name;
	}

	public final int getTestsCompletedCount() {
		return completedTests;
	}

	public final boolean isComplete() {
		return getPasses() == getTestsCompletedCount();
	}

	public final double getTestsPerSecondCount() {
		double res = 0.0;
		double totalTime = runningTimer.getTotalTime();
		double testCompleted = (double) getTestsCompletedCount();
		
		if (testCompleted == 0.0) 
			return 0.0;
			
		if (totalTime != 0.0)
			res = 1000.0 / (totalTime / testCompleted);
		else {
			// No time!!!!
			//return testCompleted;
			return Double.POSITIVE_INFINITY;
		}
		return res;
	}

	public final long getTotalWorkingTime() {
		return runningTimer.getTotalTime();
	}

	public final long getElapsedTime() {
		return elapsedTimer.getTotalTime();
	}

	public final long getTotalPauseTime() {
		return pauseTimer.getTotalTime();
	}

	public final TestFailedException[] getFailureExceptions() {
		TestFailedException[] fails = new TestFailedException[testFailedExceptions.size()];
		for (int i = 0; i < fails.length; i++)
			fails[i] = (TestFailedException) testFailedExceptions.get(i);
		return fails;
	}

	public void reset() {
		elapsedTimer = new StopWatch("elapsed timer");
		completedTests = 0;
		runningTimer = new StopWatch("running timer");
		pauseTimer = new StopWatch("pause timer");
		testFailedExceptions.clear();
	}

	
	public ResponseTimeMonitor getTargetResponse() {
	    return responseTimeMonitorImpl;
	}

	/** Run all the tests */
	public final void run() {
		//System.out.println(name + ": entering run testsToRun=" + getPasses());
		elapsedTimer.start("run");
		for (int i = 0; i < getPasses(); i++) {
			try {
				pause();
				runningTimer.start("run");
				runPass(i);
			}
			catch (Exception ex) {
				TestFailedException tfe = new TestFailedException("Failed: " + ex.getMessage(), ex);
				testFailedExceptions.add(tfe);
				onTestPassFailed(tfe);
			}
			finally {
				++completedTests;
				runningTimer.stop();
				responseTimeMonitorImpl.recordResponseTime(runningTimer.getLastInterval());
			}
		} // for each test
		elapsedTimer.stop();
	} // run

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getName() + "\t");
		sb.append(getTestsCompletedCount() + "/" + getPasses());
		sb.append("\terrs=" + getErrorCount());
		sb.append("\t" + suite.getDecimalFormat().format(getTestsPerSecondCount()) + "hps");
		sb.append("\tavg=" + responseTimeMonitorImpl.getAverageResponseTimeMillis() + "ms");
		if (longReports) {
			sb.append("\tworkt=" + getTotalWorkingTime());
			sb.append("\telt=" + getElapsedTime());
			sb.append("\best=" + responseTimeMonitorImpl.getBestResponseTimeMillis() + "ms");
			sb.append("\tworst=" + responseTimeMonitorImpl.getWorstResponseTimeMillis() + "ms");
			sb.append("\tpause=" + getTotalPauseTime());
		}
		if (isComplete() && this.longReports) {
			sb.append("\tCOMPLETED\n");
			sb.append(getErrorReport());
		}
		return sb.toString();
	}

	public final String getErrorReport() {
		String s = "";
		TestFailedException[] fails = getFailureExceptions();
		if (fails == null || fails.length == 0)
			return "No errors\n";
		else {
			for (int i = 0; i < fails.length; i++) {
				s += fails[i] + "\n";
			}
		}
		return s;
	}


	/**
	 * Run an application-specific test
	 * SPECIAL EXCEPTION THAT CAUSES TEST THREAD TO ABORT??
	 */
	protected abstract void runPass(int i) throws Exception;

	/**
	 * For handling any exceptions thrown by a Test pass. Override as desired
	 */
	protected void onTestPassFailed(TestFailedException ex) {

	}


	//---------------------------------------------------------------------
	// Convenience methods for subclassses
	//---------------------------------------------------------------------
	/**
	 * Pause for up to maxPause milliseconds
	 */
	private final void pause() {
		if (this.maxPause > 0L) {
			try {
				pauseTimer.start("pause");
				long p = Math.abs(rand.nextLong() % this.maxPause);
				Thread.sleep(p);
			}
			catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			finally {
				pauseTimer.stop();
			}
		}
	} 	// pause
	
	
	/**
	 * Convenience method to simulate delay for between min and max ms
	 * @param min minimum number of milliseconds to delay
	 * @param max maximum number of milliseconds to delay
	 */
	public static void simulateDelay(long min, long max) {
		if (max - min > 0L) {
			try {
				long p = Math.abs(min + rand.nextLong() % (max - min));
				//System.out.println("delay for " + p + "ms");
				Thread.sleep(p);
			}
			catch (InterruptedException ex) {
				// Ignore it	
			}
		}
	} 	// simulateDelay

	/** 
	 * Convenience method for subclasses
	 * @param sz size of array to index
	 * @return a random array of list index from 0 up to sz-1
	 */
	public static int randomIndex(int sz) {
		return Math.abs(rand.nextInt(sz));
	}


} // class AbstractTestThread