// Java Logging API for JDKs prior to 1.4
// Copyright (C) 2001 Brian R. Gilstrap
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details (a copy of the license
// .1 is included in the file GLPL in the doc directory of the source
// distribution).
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

// UNMODIFIED

package java14.java.util.logging;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

public class LogRecord implements Serializable {

    private static long theNextSequenceNumber = 1;

    private synchronized static long nextSequenceNumber() {
        return ++theNextSequenceNumber;
    }

    private Level theLevel;
    private String theMessage;
    private long theSequenceNumber;
    private long theMilliseconds;
    private int theThreadID;
    private String theLoggerName;
    private Object[] theParameters;
    private ResourceBundle theResourceBundle;
    private String theResourceBundleName;
    private String theSourceClassName;
    private String theSourceMethodName;
    private Throwable theThrown;
    private String theThrownBacktrace;
    private boolean guessed;

    public LogRecord( Level aLevel, String msg ) {
        theLevel = aLevel;
        theMessage = msg;
        theSequenceNumber = nextSequenceNumber();
        theMilliseconds = System.currentTimeMillis();
        theThreadID = ThreadID.get();
    }

    public Level getLevel() {
        return theLevel;
    }

    public String getLoggerName() {
        return theLoggerName;
    }

    public String getMessage() {
        return theMessage;
    }

    public synchronized long getMillis() {
        return theMilliseconds;
    }

    public Object[] getParameters() {
        return theParameters;
    }

    public ResourceBundle getResourceBundle() {
        // HERE ??? check for name and try to find bundle?
        return theResourceBundle;
    }

    public String getResourceBundleName() {
        return theResourceBundleName;
    }

    public synchronized long getSequenceNumber() {
        return theSequenceNumber;
    }

    public String getSourceClassName() {
        if ( theSourceClassName == null && ! guessed ) {
            guessClassAndMethod();
        }
        return theSourceClassName;
    }

    public String getSourceMethodName() {
        if ( theSourceMethodName == null && ! guessed ) {
            guessClassAndMethod();
        }
        return theSourceMethodName;
    }

    public int getThreadID() {
        return theThreadID;
    }

    public Throwable getThrown() {
        return theThrown;
    }

/*
    HERE - Merlin handles stack backtraces correctly. How do we adjust for
    this? Especially, how do we reconcile what we do with what Merlin does so
    that we can have a program using RMI and Lumberjack talk successfully with
    a program using Merlin and RMI???
    public String getThrownBackTrace() {
        return theThrownBacktrace;
    }
*/
    public void setLevel( Level aLevel ) {
        theLevel = aLevel;
    }

    public void setLoggerName( String aLoggerName ) {
        theLoggerName = aLoggerName;
    }

    public void setMessage( String aMessage ) {
        theMessage = aMessage;
    }

    public synchronized void setMillis( long l ) {
        theMilliseconds = l;
    }

    public void setParameters( Object[] parameters ) {
        theParameters = parameters;
    }

    public void setResourceBundle( ResourceBundle bundle ) {
        theResourceBundle = bundle;
        // HERE - set resource bundle name ?
    }

    public void setResourceBundleName( String name ) {
        theResourceBundleName = name;
        // HERE - set resource bundle?
    }

    public synchronized void setSequenceNumber( long n ) {
        theSequenceNumber = n;
    }

    public void setSourceClassName( String aName ) {
        theSourceClassName = aName;
    }

    public void setSourceMethodName( String aName ) {
        theSourceMethodName = aName;
    }

    public void setThreadID( int anID ) {
        theThreadID = anID;
    }

    public void setThrown( Throwable t ) {
        theThrown = t;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        t.printStackTrace( pw );
        pw.close();
        theThrownBacktrace = sw.getBuffer().toString();
    }

	/** Hacky and slow. This is more efficient in 1.4.
	 * However, it should only be called during development.
	 **/
    private void guessClassAndMethod() {
    	
    	//System.out.println("GUESS CLASS AND METHOD - SLOW SLOW SLOW!!!!!");
    	
        try {
            Throwable t = new Throwable();
            t.fillInStackTrace();
            StringWriter w = new StringWriter();
            PrintWriter pw = new PrintWriter( w );
            t.printStackTrace( pw );
            StringTokenizer tokenizer = new StringTokenizer( w.getBuffer().toString(), "\n" );
            tokenizer.nextToken();
            String line = tokenizer.nextToken();
			// Find the Logger - the caller had to call Logger...
            while ( line.indexOf( "java.util.logging.Logger" ) == -1 ) {
           // while ( line.indexOf( "com.interface21.logging.Logger" ) == -1 ) {
                line = tokenizer.nextToken();
            }
			// Now move past all the Logger methods to get to the
			// caller of the Logger.
			while ( line.indexOf( "java.util.logging.Logger" ) >= 0 ) {
			//while ( line.indexOf( "com.interface21.logging.Logger" ) >= 0 ) {
				line = tokenizer.nextToken();
			}
            int start = line.indexOf( "at " ) + "at ".length();
            int end = line.indexOf( '(' );
            String temp = line.substring( start, end );
            int lastPeriod = temp.lastIndexOf( '.' );
            theSourceClassName = temp.substring( 0, lastPeriod );
            theSourceMethodName = temp.substring( lastPeriod + 1 );
        }
        catch ( Exception e ) {
            // Ignore and let us return an empty string
            //e.printStackTrace( System.err );
        }
        guessed = true;
    }

	/**
	* The unique (within a JVM) Thread ID for Log Records.
	* <p>
	* Straight out of the Thread Local example - and right for this
	* application. Each thread should get its own unique ID, and this will
	* ensure that it does. 
	*/
	private static class ThreadID {
		/**
		* The next thread ID to be assigned.
		* <p>
		* The value starts at <code>10</code>, presumably because there are
		* eight or so threads started whenever the JVM starts.  By
		* observation, the value is initialized this way in the Sun
		* implementation, with no explanation given. 
		*/
		private static int nextThreadID = 10;
 
		private static ThreadLocal threadID = new ThreadLocal() {
				protected synchronized Object initialValue() {
					return new Integer(nextThreadID++);
				}
			};
 
		public static int get() {
			return ((Integer) (threadID.get())).intValue();
		}
	}
}
