
package com.interface21.load;

import com.interface21.util.ResponseTimeMonitor;

/**
 * run() from Runnable interface actually runs the test
 * @author  Rod Johnson
 * @since February 9, 2001
 */
public interface Test extends Runnable {
	
	void setName(String name);
	
	// Init
	void setTestSuite(AbstractTestSuite ts);
	
	void setInstances(int count);
	
	int getInstances();
	
	void setFixture(Object context) ;

	// These methods can be queried during work
    int getTestsCompletedCount();
    String getName();
    int getErrorCount();
    boolean isComplete();
    int getPasses();
    
    void setLongReports(boolean flag);
    
    long getMaxPause();
    void setMaxPause(long p);
    
    void setPasses(int count);
    
    double getTestsPerSecondCount();
    
    long getElapsedTime();
    long getTotalWorkingTime();
    
    TestFailedException[] getFailureExceptions();
    
    long getTotalPauseTime();
    //ResponseTimeMonitoringAuditedResource getTargetResponse();
    
    ResponseTimeMonitor getTargetResponse();

    void reset();
}

