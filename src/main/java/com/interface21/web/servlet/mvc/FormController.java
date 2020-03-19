package com.interface21.web.servlet.mvc;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.interface21.validation.BindException;
import com.interface21.validation.DataBinder;
import com.interface21.validation.Validator;
import com.interface21.web.bind.WebRequestBindingException;
import com.interface21.web.bind.HttpServletRequestDataBinder;
import com.interface21.web.servlet.ModelAndView;

/**
 * The Struts approach with autopopulation
 * Automatically goes to resubmit viewname on failure.
 * ASSUMPTIONS: forms are submitted by POST
 * 
 * HOW USEFUL IS IT CONCRETE? what can we do with a processed request!?
 */
public class FormController extends BaseCommandController {

	private String resubmitView;

	// set form view!? defaults to resubmit

	private String successView;

	/** 
	 * Subclasses must provide a no arg constructor that invokes this
	 * to provide the command class
	 */
	public FormController(Class commandClass, String beanName) {
		setCommandClass(commandClass);
		setBeanName(beanName);
	}
	
	public FormController(Class commandClass) {
		setCommandClass(commandClass);
	}
	
	
	public FormController() {
	}

	
	public final void setFormView(String resubmitView) {
		this.resubmitView = resubmitView;
	}
	
	public final void setSuccessView(String successView) {
		this.successView = successView;
	}
	
	protected final String getSuccessView() {
		return this.successView;
	}
	
	// INIT TO CHECK PROPERTIES

	/**
	 * Can override
	 * COULD USE SPECIAL REQUEST PARAMETER on form: added by bind: tag?
	 */
	protected boolean isFormSubmission(HttpServletRequest request) {
		return "POST".equals(request.getMethod());
	}

	/**
	 * @see AbstractController#handleRequestInternal(HttpServletRequest, HttpServletResponse)
	 */
	protected final ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		if (isFormSubmission(request)) {
			return submission(request, response);
		}
		else {
			return form(request, response);
		}
	}

	private ModelAndView form(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		logger.info("Displaying form");
		Map model = referenceDataModel(request);
		model.put(getBeanName(), formBackingObject(request));
		return new ModelAndView(this.resubmitView, model);
	}

	/**
	 * Subclasses can override this to set reference data used on 
	 * form
	 * @param request request -- in case we care about locale
	 */
	protected Map referenceData(HttpServletRequest request) {
		return null;
	}
	
	
	/** Allow referenceData to return null */
	private Map referenceDataModel(HttpServletRequest request) {
		Map model = referenceData(request);
		if (model == null) {
			model = new HashMap();
		}
		return model;
	}

	/**
	 * Subclasses can override this
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		return userObject(request);
	}

	/**
	 * Handle form submission
 	*/
	private ModelAndView submission(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		logger.info("Handling form submission");
		Object command = userObject(request);
		DataBinder errors = bindAndValidate(request, command);
		if (!errors.hasErrors()) {
			return onSubmit(request, response, command, errors);
		}
		else {
			logger.warning("Data binding exception " + errors);
			Map model = referenceDataModel(request);
			model.putAll(errors.getModel());
			ModelAndView mv = new ModelAndView(this.resubmitView, model);
			return mv;
		}
	}

	/**
	 * Allow subclass to throw out the input and go to a form
	 */
	protected final ModelAndView inputForm(HttpServletRequest request, HttpServletResponse response, DataBinder binder) {
		ModelAndView mv = new ModelAndView(this.resubmitView, binder.getModel());
		return mv;
	}

	/**
	 * OVERRIDE THIS TO GET A CRACK AT COMMAND and stop calling handleValidCommand.
	 * Default is to invoke handleValidCommand
	 * @param command Command with request parameters bound onto it
	 * @param binder binder without errors. Subclass can add errors if it wants to
	 */
	protected ModelAndView onSubmit(
		HttpServletRequest request,
		HttpServletResponse response,
		Object command,
		DataBinder binder)
		throws ServletException, IOException {
		return onSubmit(command, binder);
	}

	/**
	 */
	protected ModelAndView onSubmit(Object command, DataBinder binder) throws ServletException {
		return onSubmit(command);
	}
	
	/**
	 * Simplest
	 */
	protected ModelAndView onSubmit(Object command) throws ServletException {
		if (this.successView == null)
			throw new ServletException("Cannot use default onSubmit: successView isn't set");
		return new ModelAndView(getSuccessView(), getBeanName(), command);
	}
	
	
	
	/*
	public class FormInfo implements Serializable {
		public int source;
		
		public void isNewInput() {
		}
		
		public void isResubmit() {
		}
		
		public void isSubmission() {
		}
		
	}
	*/

}