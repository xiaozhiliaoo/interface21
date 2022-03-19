
package com.interface21.ejb.access;

import com.interface21.beans.BeanWrapper;
import com.interface21.beans.BeanWrapperImpl;

import javax.ejb.EJBLocalObject;

/*
 * Simply wraps the local reference.
 * This is a simple but questionable approach;
 * we don't really want to keep invoking the same EJB object,
 * as there are potential stateleness issues.
 * @author Rod Johnson
 */
public class LocalStatelessSessionReferenceBeanDefinition extends AbstractLocalStatelessSessionBeanDefinition {
	
	/**
	 * Invoked when home is available.
	 * Once configuration is complete, callers will have a direct
	 * reference to the EJB.
	 */
	protected BeanWrapper newBeanWrapperInternal() {
		EJBLocalObject sessionEjb = newSessionBeanInstance();
		logger.config("Returning BeanWrapper for stateless session EJB [" + sessionEjb + "]");
		return new BeanWrapperImpl(sessionEjb);
	}

}
