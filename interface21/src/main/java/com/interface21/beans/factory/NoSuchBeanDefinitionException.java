/*
 * NoSuchPrototypeException.java
 *
 * Created on 16 April 2001, 09:52
 */

package com.interface21.beans.factory;
import com.interface21.beans.*;

/**
 *
 * @author  johnsonr
 * @version 
 */
public class NoSuchBeanDefinitionException extends BeansException {
	
	 /** CVS id
	  */
	public static final String REVISION_ID = "$Id: NoSuchBeanDefinitionException.java,v 1.1.1.1 2002/08/01 12:52:02 Rod Johnson Exp $";
    
    private String name;

    /**
    * Creates new <code>NoSuchPrototypeException</code> without detail message.
     */
    public NoSuchBeanDefinitionException(String name) {
        super("No bean named [" + name + "] is defined", null);
        this.name = name;
    }
	
	 public NoSuchBeanDefinitionException(String name, String message) {
		super("No bean named [" + name + "] is defined {" + message + "}", null);
        this.name = name;
    }
    
    public String getBeanName() {
        return name;
    }
    
}


