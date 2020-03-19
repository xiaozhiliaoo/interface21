package com.interface21.web.servlet.mvc;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.interface21.validation.BindException;
import com.interface21.validation.DataBinder;
import com.interface21.validation.Validator;
import com.interface21.validation.Errors;
import com.interface21.web.bind.WebRequestBindingException;
import com.interface21.web.bind.HttpServletRequestDataBinder;
import com.interface21.web.servlet.ModelAndView;

/**
 * Extensions of AbstractController to 
 * have the notion of a Validator,
 * a command bean
 * Internal to this package.
 */
public abstract class BaseCommandController extends AbstractController {

	private Class commandClass;

	private Validator validator;
	
	private String beanName;

	
	public BaseCommandController() {
	}
	
	protected void setCommandClass(Class commandClass) {
		this.commandClass = commandClass;
	}

	
	public final void setCommandClassName(String name) throws ClassNotFoundException {
		this.commandClass = Class.forName(name);
	}
	
	public final void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	protected final String getBeanName() {
		return this.beanName;
	}
	

	public final void setValidator(Validator validator) throws IllegalArgumentException {
		if (!validator.supports(this.commandClass))
			throw new IllegalArgumentException(
				"Validator [" + validator + "] can't validate command class of type " + commandClass);
		this.validator = validator;
	}

	
	
	
	
	/**
	 * Subclasses can override this
	 * @return object to bind onto
	 */
	protected Object userObject(HttpServletRequest request) throws ServletException {
		logger.info("Must create new command of " + commandClass);
		try {
			Object command = commandClass.newInstance();
			return command;
		}
		catch (Exception ex) {
			throw new ServletException(
				"Cannot instantiate command " + commandClass + "; does it have a public no arg constructor?",
				ex);
		}
	}
	
	
	// *TODO: must be able to parameterize the binding process.
	// without depending on DataBinder
	
	protected final DataBinder bindAndValidate(HttpServletRequest request, Object command) throws ServletException {
		HttpServletRequestDataBinder binder = new HttpServletRequestDataBinder(command, this.beanName);
		binder.bind(request);

		
		// TODO: do we still want to invoke validator!????
		// it would need to check if there already was an error for each field

		if (this.validator != null) {
			logger.info("Invoking validator [" + this.validator + "]");
			validator.validate(command, binder);
			if (binder.hasErrors()) {
				logger.info("Validator found " + binder.getErrorCount() + " errors: going to resubmit form");
					
			}
			else {
				logger.fine("Validator found no errors");
			}
		}
		
		// May throw exception
		// DO WE WANT THIS? we may still want to invoke validator...
		//binder.close();
		
		return binder;		
	}


}