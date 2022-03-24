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

package org.springframework.web.servlet.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.servlet.support.JspAwareRequestContext;
import org.springframework.web.servlet.support.RequestContext;

/**
 * Superclass for all tags that require a RequestContext.
 * The RequestContext instance provides easy access to current
 * state like WebApplicationContext, Locale, Theme, etc.
 *
 * <p>Mainly intended for DispatcherServlet requests;
 * will use fallbacks when used outside DispatcherServlet.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see RequestContext
 * @see org.springframework.web.servlet.DispatcherServlet
 */
public abstract class RequestContextAwareTag extends TagSupport implements TryCatchFinally {

	/** PageContext attribute for page-level RequestContext instance */
	public static final String REQUEST_CONTEXT_PAGE_ATTRIBUTE =
			"org.springframework.web.servlet.tags.REQUEST_CONTEXT";

	protected final Log logger = LogFactory.getLog(getClass());

	private RequestContext requestContext;


	/**
	 * Create and expose the current RequestContext.
	 * Delegates to <code>doStartTagInternal</code> for actual work.
	 * @see #doStartTagInternal
	 * @see #REQUEST_CONTEXT_PAGE_ATTRIBUTE
	 * @see JspAwareRequestContext
	 */
	public final int doStartTag() throws JspException {
		this.requestContext = (RequestContext) this.pageContext.getAttribute(REQUEST_CONTEXT_PAGE_ATTRIBUTE);
		try {
			if (this.requestContext == null) {
				this.requestContext = new JspAwareRequestContext(this.pageContext);
				this.pageContext.setAttribute(REQUEST_CONTEXT_PAGE_ATTRIBUTE, this.requestContext);
			}
			return doStartTagInternal();
		}
		catch (JspException ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
		catch (RuntimeException ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
		catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw new JspTagException(ex.getMessage());
		}
	}

	/**
	 * Return the current RequestContext.
	 */
	protected final RequestContext getRequestContext() {
		return requestContext;
	}

	/**
	 * Called by doStartTag to perform the actual work.
	 * @return same as TagSupport.doStartTag
	 * @throws Exception any exception, any checked one other than
	 * a JspException gets wrapped in a JspException by doStartTag
	 * @see TagSupport#doStartTag
	 */
	protected abstract int doStartTagInternal() throws Exception;


	public void doCatch(Throwable throwable) throws Throwable {
		throw throwable;
	}

	public void doFinally() {
		this.requestContext = null;
	}

}
