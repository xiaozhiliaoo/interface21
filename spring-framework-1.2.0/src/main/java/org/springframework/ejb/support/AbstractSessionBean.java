/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ejb.support;

import javax.ejb.SessionContext;

/**
 * Superclass for all session beans. Not intended for direct client subclassing;
 * derive from AbstractStatelessSessionBean or AbstractStatefulSessionBean instead.
 *
 * <p>This class saves the session context provided by the EJB container in an instance
 * variable and provides a NOP implementation of the ejbRemove() lifecycle method.
 *
 * @author Rod Johnson
 * @see AbstractStatelessSessionBean
 * @see AbstractStatefulSessionBean
 */
abstract class AbstractSessionBean extends AbstractEnterpriseBean implements SmartSessionBean {

	/** the SessionContext passed to this object */
	private SessionContext sessionContext;

	/**
	 * Set the session context.
	 * <p><b>If overriding this method, be sure to invoke this form of it first.</b>
	 * @param sessionContext SessionContext context for session
	 */
	public void setSessionContext(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}
			
	/**
	 * Convenience method for subclasses.
	 * Return the EJB context saved on initialization.
	 * @return the SessionContext saved on initialization by this class's
	 * implementation of the setSessionContext() method.
	 */
	public final SessionContext getSessionContext() {
		return sessionContext;
	}
	
}
