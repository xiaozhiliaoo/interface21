package com.interface21.load;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 *
 * @author  Rod Johnson
 * @since February 9, 2001
 */
public abstract class AbstractTestSuite {

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	private long startTime;

	private long maxPause;

	private long reportInterval;

	private Test[] tests;
	
	private int numTests;
	
	private int numWorkerThreads;
	
	private String name;
	
	private String reportFile;
	
	private boolean longReports;
	
	private DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
	
	private Object context;

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/** Creates a new AbstractThreadedTest */
	public AbstractTestSuite() {
		df.applyPattern("###.##");
	}
	
	
	/**
	 * Gets the name.
	 * @return Returns a String
	 */
	public String getName() {
		return name;
	}
	
	
	public final DecimalFormat getDecimalFormat() {
		return df;
	}
	
	public final void setDoubleFormat(String pattern) {
		df.applyPattern(pattern);
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public final void setLongReports(boolean longReports) {
		this.longReports = longReports;
	}

	public final boolean getLongReports() {
		return longReports;
	}
	
	public void setFixture(Object context) {
		this.context = context;
	}
	
		/**
	 * Gets the reportFile.
	 * @return Returns a String
	 */
	public String getReportFile() {
		return reportFile;
	}

	/**
	 * Sets the reportFile.
	 * @param reportFile The reportFile to set
	 */
	public void setReportFile(String reportFile) {
		this.reportFile = reportFile;
	}
	

	public int getTotalPassCount() {
		int passCount = 0;
		Test[] tests = getTests();
		if (tests != null) {
			for (int i = 0; i < tests.length; i++) {
				passCount += tests[i].getPasses();
			}
		}

		return passCount;
	}

	public void runAllTests(boolean blockTilComplete) {

		Test[] tests = getTests();
		Thread[] runners = new Thread[tests.length];

		//System.out.println("Will run " + runners.length + " tests");

		int nbTests = tests.length;

		// instantiate the Threads the run them - we do not want our timing to
		// be augmented by the instantantion overhead
		for (int i = 0; i < nbTests; i++) {
			tests[i].reset();
			runners[i] = new Thread(tests[i]);
		}

		for (int i = 0; i < nbTests; i++) {
			//System.out.println("Starting thread " + i);
			runners[i].start();
		}
		
		// Use a Java 1.3 Timer to do periodic reporting
		if (this.reportInterval > 0L) {
			Timer timer = new Timer();
			timer.schedule(new ReportTimerTask(), getReportInterval(), getReportInterval());
			System.out.println("Reporting every " + getReportInterval() + "ms");
		}

		if (blockTilComplete) {
			for (int i = 0; i < nbTests; i++) {
				try {
					runners[i].join();
				}
				catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	
	private class ReportTimerTask extends TimerTask { 
		/**
		 * @see Runnable#run()
		 */
		public void run() {
			AbstractTestSuite.this.report();
			if (AbstractTestSuite.this.isComplete()) {
				cancel();
				if (reportFile != null) {
					reportToFile();
				}
				System.exit(0);
			}
		}
	}

	public Test[] getTests() {
		if (tests == null) {
			tests = createTests();
			numWorkerThreads = tests.length;
			for (int i = 0; i < tests.length; i++) {
				String classname = tests[i].getClass().getName();
				classname = classname.substring(classname.lastIndexOf(".") + 1);
				tests[i].setName(classname + "-" + i);
				tests[i].setTestSuite(this);
				tests[i].setFixture(this.context);
			}
		}
		return tests;
	}

	protected abstract Test[] createTests();

	public int getThreads() {
		if (tests == null)
			return numWorkerThreads;
		return tests.length;
	}

	public int getPassesCompleted() {
		int passesCompleted = 0;
		Test[] t = getTests();
		int nbTests = t.length;

		for (int i = 0; i < nbTests; i++)
			passesCompleted += t[i].getTestsCompletedCount();
		return passesCompleted;
	}

	public void clearResults() {
		Test[] allTests = getTests();
		for (int i = 0; i < allTests.length; i++)
			allTests[i].reset();
	}

	//---------------------------------------------------------------------
	// Additional bean properties subclasses may want to use
	//---------------------------------------------------------------------
	public long getMaxPause() {
		return maxPause;
	}

	public void setMaxPause(long maxPause) {
		this.maxPause = maxPause;
	}

	public long getReportInterval() {
		return reportInterval;
	}

	public void setReportInterval(long reportInterval) {
		this.reportInterval = reportInterval;
	}
	
	public void setReportIntervalSeconds(int reportIntervalSecs) {
		this.reportInterval = reportIntervalSecs * 1000L;
	}


	/** Only use if desired */
	public final void setThreads(int numWorkerThreads) {
		this.numWorkerThreads = numWorkerThreads;
	}

	/** Subclasses can use this as a convenience if all their test threads
	 * have the same number of tests */
	public final void setPasses(int numTests) {
		this.numTests = numTests;
	}

	public final int getPasses() {
		return numTests;
	}
	
	public String toString() {
		return "Test suite name='" + name + "': numTests=" + numTests + "; numberOfWorkerThreads=" + numWorkerThreads + 
			"; maxPause=" + maxPause;
	}

	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------
	public final boolean isComplete() {
		for (int i = 0; i < getTests().length; i++) {
			if (!getTests()[i].isComplete())
				return false;
		}
		return true;
	}

	public void report() {
		report(new PrintWriter(System.out));
	}

	public void report(PrintWriter pw) {
		StringBuffer sb = new StringBuffer();
		report(sb);
		pw.println(sb.toString());
		pw.flush();
	}

	public void report(StringBuffer sb) {
		sb.append("-----------------------------------\n");
		Test[] allTests = getTests();
		int totalResponseTimeAvg = 0;
		int totalHits = 0;
		double hps = 0.0;
		for (int i = 0; i < allTests.length; i++) {
			sb.append(allTests[i]).append("\n");
			totalResponseTimeAvg += allTests[i].getTargetResponse().getAverageResponseTimeMillis();
			hps += allTests[i].getTestsPerSecondCount();
			totalHits += allTests[i].getTestsCompletedCount();
		}
		
		int avgResponseTime = totalResponseTimeAvg / allTests.length;
		sb.append("*********** Total hits=" + totalHits + "\n");
		sb.append("*********** HPS=" + df.format(hps) + "\n");
		sb.append("*********** Average response=" + avgResponseTime + "\n");
	}
	
	private void reportToFile() {
		if (this.reportFile != null) {
			try {
				OutputStream os = new FileOutputStream(this.reportFile);
				PrintWriter pw = new PrintWriter(os);
				pw.println(this);
				report(pw);
				os.close();
				System.out.println("Wrote report file");
			}
			catch (IOException ex) {
				System.out.println("Couldn't write report file: " + ex);
				ex.printStackTrace();
			}
		}
	}

} // class AbstractThreadedTest