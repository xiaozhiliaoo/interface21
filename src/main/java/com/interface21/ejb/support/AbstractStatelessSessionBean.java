/**
 * Feel free to modify this code to meet your needs.
 * These packages are described in "Expert One-on-One
 * J2EE Design and Development" by Rod Johnson (Wrox, 2002).
 * 
 * For commercial support, contact rod.johnson@interface21.com.
 */

package com.interface21.ejb.support;


import javax.ejb.CreateException;
import javax.ejb.EJBException;

/**
 * Accommodates stateless requirements:
 * overrides activate and passivate.
 */
public abstract class AbstractStatelessSessionBean extends AbstractSessionBean {

	/** 
	 * This is declared abstract to ensure that subclasses implement this. 
	 * Otherwise it isn't required by the compiler, but will
	 * fail on deployment.
	 * <br/>The BeanFactory is available at this point
	 */
	public abstract void ejbCreate() throws CreateException;
	
	/**
	 * @see SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException {
		throw new IllegalStateException("ejbActivate must not be invoked on a stateless session bean");
	}

	/**
	 * @see SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException {
		throw new IllegalStateException("ejbPassivate must not be invoked on a stateless session bean");
	}

}
