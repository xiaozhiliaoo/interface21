
package com.interface21.beans;

/**
 * Thrown on an unrecoverable problem encountered in the
 * beans packages or sub-packages: e.g. bad class or field
 * @author  Rod Johnson
 * @version $Revision: 1.1.1.1 $
 */
public class FatalBeanException extends BeansException {

    /**
 * Constructs an <code>InvalidTargetException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public FatalBeanException(String msg,Throwable t) {
        super(msg, t);
    }
}


