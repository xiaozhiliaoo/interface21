package com.interface21.core;

import java.io.PrintStream;
import java.io.PrintWriter;


// IMPLEMENTS ERRORCODED?

/**
 * Handy class for wrapping Exceptions so to allow a method to throw
 * just the wrapper and not have to throw a whole bunch of Exceptions.
 * (a la RemoteException)
 * printStackTrace() etc. are forwarded to the wrapped Exception.
 * Abstact to force the programmer to extend the class
 * @author Rod Johnson
 * @version $Id: NestedRuntimeException.java,v 1.1.1.1 2002/08/01 12:52:03 Rod Johnson Exp $
 */
public abstract class NestedRuntimeException extends RuntimeException implements HasRootCause {

   /**
    * Nested Exception to hold wrapped exception.
    */
    private Throwable rootCause;
    
    
   /**
    * Constructs a <code>ExceptionWrapperException</code> with the specified
    * detail message.
    * @param s the detail message
    */
    public NestedRuntimeException(String s) {
        super(s);
    }		
    
    
   /**
    * Constructs a <code>RemoteException</code> with the specified
    * detail message and nested exception.
    *
    * @param s the detail message
    * @param ex the nested exception
    */
    public NestedRuntimeException(String s, Throwable ex) {
        super(s);
        rootCause = ex;
    }
	
	    
	
	/** May be null */
	public Throwable getRootCause() {
		return rootCause;
	}
   
  /**
     * Returns the detail message, including the message from the nested
     * exception if there is one.
     */
    public String getMessage() {
        if (rootCause == null)
            return super.getMessage();
        else
            return super.getMessage() + "; nested exception is: \n\t" + rootCause.toString();
    }
    
   /**
    * Prints the composite message and the embedded stack trace to
    * the specified stream <code>ps</code>.
    * @param ps the print stream
    */
    public void printStackTrace(PrintStream ps) {

        if (rootCause == null) {
            super.printStackTrace(ps);
        }
        else {
            ps.println(this);
            rootCause.printStackTrace(ps);
        } // else

    }
	
	 /**
    * Prints the composite message and the embedded stack trace to
    * the specified print writer <code>pw</code>
    * @param pw the print writer
    */
    public void printStackTrace(PrintWriter pw) {

        if (rootCause == null) {
            super.printStackTrace(pw);
        }
        else {     
			pw.println(this);
			rootCause.printStackTrace(pw);
        }

    }
    
   /**
    * Prints the composite message to <code>System.err</code>.
    */
    public void printStackTrace() {
        printStackTrace(System.err);
       // printStackTrace(System.out);
    }
  
    
}	// ExceptionWrapperException
