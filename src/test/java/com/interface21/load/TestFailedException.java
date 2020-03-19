package com.interface21.load;



import com.interface21.core.NestedCheckedException;

public class TestFailedException extends NestedCheckedException {

   /**
    * The CVS version number.
    */
   public static final String REVISION_ID ="$Id: TestFailedException.java,v 1.3 2001/04/23 10:41:37 allanm Exp $";


   public TestFailedException( String message ) {
      super( message );
   }


   public TestFailedException( String message, Throwable t) {
      super( message, t);
   }

}
