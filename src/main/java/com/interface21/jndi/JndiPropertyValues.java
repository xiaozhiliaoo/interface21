

package com.interface21.jndi;

import com.interface21.beans.MutablePropertyValues;
import com.interface21.beans.PropertyValue;
import com.interface21.beans.PropertyValues;
import com.interface21.util.StringUtils;
import java14.java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;

/**
 * PropertyValues implementation created from ServetConfig parameters.
 * <br/>This class is immutable once initialized.
 * @author Rod Johnson
 * @version $RevisionId$
 * NOT CURRENTLY USED
 */
public class JndiPropertyValues implements PropertyValues {
	
	public static final String DELIMITER = ".";
	
	private static final Logger logger = Logger.getLogger(JndiPropertyValues.class.getName());
	
	/** PropertyValues delegate. We use delegation rather than simply subclass
	 * MutablePropertyValues as we don't want to expose MutablePropertyValues's
	 * update methods. This class is immutable once initialized.
	 */
	private MutablePropertyValues mutablePropertyValues;
	
	/** Creates new PropertyValues object
	 * @param name without java:comp/env - e.g. logger
	 * @throws NamingException
	 */
	public JndiPropertyValues(String name) throws NamingException {
		this(name, null);
	}
	
	/** Creates new PropertyValues object
	 * @param requiredProperties array of property names we need, where
	 * we can't accept default values
	 * @throws NamingException if any required properties are missing. It isn't
	 * an error to find no properties
	 */
	public JndiPropertyValues(String name, List requiredProperties) throws NamingException {
		// Ensure we have a deep copy
		List missingProps = (requiredProperties == null) ? new ArrayList(0) : new ArrayList(requiredProperties);		
		mutablePropertyValues = new MutablePropertyValues();
		
		Context initCtx = null;		
		try {
			initCtx = new InitialContext( );
			/*NamingEnumeration enum = initCtx.listBindings("java:comp/env");
			
			// At least Orion 1.5.2 doesn't seem to regard anything under a /
			// as a true subcontext, so we need to search all bindings
			// Not all that fast, but it doesn't matter				
			while (enum.hasMore()) {
				Binding binding = (Binding) enum.next();								
				logger.finest("Found JNDI binding: Name: " + binding.getName( ));
				logger.finest("Type: " + binding.getClassName( ));
				logger.finest("Value: " + binding.getObject( ));
								
				if (binding.getName().indexOf(name + DELIMITER) != -1) {
					// Found one
					// We know there's a last slash
					int lastSlash = binding.getName().lastIndexOf(DELIMITER);
					
					String property = binding.getName().substring(lastSlash + 1);
					if (!"class".equals(property)) {
						Object value = binding.getObject();
						logger.finest("----- FOUND NAME [" + property + "] with value=[" + value + "]");
						mutablePropertyValues.addPropertyValue(new PropertyValue(property, value));
						// Check it off
						missingProps.remove(property);
					}
				}
			}
			enum.close();*/
		}
		catch (NamingException ex) {
			logger.fine("----- NO PROPERTIES FOUND " + ex);
		}
		finally {
			initCtx.close();
		}
		
		
		// Fail if we are still missing properties
		if (missingProps.size() > 0) {
			throw new NamingException("Initialization from JNDI failed: the following required properties were missing -- (" +
			StringUtils.collectionToDelimitedString(missingProps, ", ") + ")");
		}
		
		logger.fine("Found PropertyValues in ServletConfig: " + mutablePropertyValues);
	}	// constructor
	
	
	/** Return an array of the PropertyValue objects
	 * held in this object.
	 * @return an array of the PropertyValue objects
	 * held in this object.
	 */
	public PropertyValue[] getPropertyValues() {
		// We simply let the delegate handle this
		return mutablePropertyValues.getPropertyValues();
	}
	
	/** Is there a propertyValue object for this property?
	 * @param propertyName name of the property we're interested in
	 * @return whether there is a propertyValue object for this property?
	 */
	public boolean contains(String propertyName) {
		return mutablePropertyValues.contains(propertyName);
	}
	
	public PropertyValue getPropertyValue(String propertyName) {
		return mutablePropertyValues.getPropertyValue(propertyName);
	}
	
}	// JNDIPropertyValues
