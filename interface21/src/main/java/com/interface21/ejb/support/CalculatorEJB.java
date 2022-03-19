
package com.interface21.ejb.support;

import javax.ejb.CreateException;

public class CalculatorEJB extends AbstractStatelessSessionBean implements Calculator {
	
	//private MyHelper myHelper;

	/**
	 * @see AbstractStatelessSessionBean#ejbCreate()
	 */
	public void ejbCreate() throws CreateException {
		//this.myHelper = (MyHelper) getBeanFactory().getBean("myHelper");
	}

	/**
	 * @see Calculator#getUncacheableValue()
	 */
	public int getUncacheableValue() {
		return 0;
	}

	/**
	 * @see Calculator#getCacheableValue()
	 */
	public int getCacheableValue() {
		return 0;
	}

}
