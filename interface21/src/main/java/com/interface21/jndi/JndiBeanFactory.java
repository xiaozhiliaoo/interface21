package com.interface21.jndi;

import com.interface21.beans.BeansException;
import com.interface21.beans.factory.support.ListableBeanFactoryImpl;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;

/**
 * <b>Note well: Do not call getAsSingleton() from EJBs.
 * This method is synchronized.</b>
 * DELEGATE TO DISABLE IT WITH UNSUPPORTED OPERATION EXCEPTION?
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class JndiBeanFactory extends ListableBeanFactoryImpl {
	
	/** Syntax is beans.name.class=Y */
	public static final String BEANS_PREFIX = "beans.";
	
	
	public static final String DELIMITER = ".";

	/** Creates new JNDIBeanFactory
	 * @param root likely to be "java:comp/env"
	 */
    public JndiBeanFactory(String root) throws BeansException {
		
		// We'll take everything from the NamingContext and dump it in a
		// Properties object, so that the superclass can efficiently manipulate it
		// after we've closed the context.
		HashMap m = new HashMap();
		
		Context initCtx = null;		
		try {
			initCtx = new InitialContext();
			// Parameterize
//			NamingEnumeration enum = initCtx.listBindings(root);
			
/*
			// Orion 1.5.2 doesn't seem to regard anything under a /
			// as a true subcontext, so we need to search all bindings
			// Not all that fast, but it doesn't matter				
			while (enum.hasMore()) {
				Binding binding = (Binding) enum.next();								
				logger.finest("Name: " + binding.getName( ));
				logger.finest("Type: " + binding.getClassName( ));
				logger.finest("Value: " + binding.getObject());								
				m.put(binding.getName(), binding.getObject());
			}
			enum.close();
*/

			registerBeanDefinitions(m, BEANS_PREFIX);
		}
		catch (NamingException ex) {
			logger.fine("----- NO PROPERTIES FOUND " + ex);
		}
		finally {
			try {
				if (initCtx != null) {
					initCtx.close();
				}
			}
			catch (NamingException ex) {
				// IGNORE OR THROW RTE?
			}
		}
    }	// constructor
	
}	// class JNDIBeanFactory
