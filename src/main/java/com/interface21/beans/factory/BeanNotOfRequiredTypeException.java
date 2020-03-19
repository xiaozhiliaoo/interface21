
package com.interface21.beans.factory;

import com.interface21.beans.*;

/**
 * Thrown when a bean doesn't match the required type
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class BeanNotOfRequiredTypeException extends BeansException {
		
    private String name;
	
	private Class requiredType;
	
	private Object actualInstance;

    /**
    * Creates new <code>NoSuchPrototypeException</code> without detail message.
     */
    public BeanNotOfRequiredTypeException(String name, Class requiredType, Object actualInstance) {
        super("Bean named [" + name + "] must be of type " + requiredType.getName() + ", but was actually of type " + actualInstance.getClass().getName(), null);
        this.name = name;
		this.requiredType = requiredType;
		this.actualInstance = actualInstance;
    }
	
    
    public String getBeanName() {
        return name;
    }
	
	public Class getRequiredType() {
		return requiredType;
	}
		
	public Class getActualType() {
		return actualInstance.getClass();
	}
	
	public Object getActualInstance() {
		return actualInstance;
	}		
    
}


