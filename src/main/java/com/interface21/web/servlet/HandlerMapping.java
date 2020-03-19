package com.interface21.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.interface21.beans.factory.ListableBeanFactory;
import com.interface21.context.ApplicationContextAware;
import com.interface21.context.ApplicationContextException;

/**
 * Interface to be implemented by objects that define a mapping between
 * requests and handler objects.  
 * <br/>This class can be implemented by application developers,
 * although this is not necessary, as the default URLHandlerMapping class
 * is available if no custom handlers are registered.
 * @author Rod Johnson
 * @version $RevisionId$
 */
public interface HandlerMapping extends ApplicationContextAware {
	
	/** 
	 * Will be invoked <b>after</b> setApplicationContext().
	 * Handler objects are normally taken from the BeanFactory.
	 * This means that implementing classes should never need to open
	 * configuration files for themselves.
	 * @throws ApplicationContextException if this handler map cannot
	 * initialize correctly. Any root cause should be nested within this
	 * exception.
	 */
	void init() throws ApplicationContextException;
	
	/**
	 * Return a handler for this request.
	 * The choice may be made on request URL, session state 
	 * or any factor the implementing class chooses.
	 * @return a handler object. This returns Object, rather than even a tag
	 * interface, so that handlers are not constrained. For example, a HandlerAdapter
	 * could be written to allow another framework's handler objects to be used.
	 * Returns null if no match was found. This is not an error.
	 * The controllerServlet will query all registered HandlerMap objects
	 * to find a match, and only decide there is an error if none
	 * can find a handler.
	 * @throws ServletException if there is an internal error
	 */
	Object getHandler(HttpServletRequest request) throws ServletException;

}	// interface HandlerMap

