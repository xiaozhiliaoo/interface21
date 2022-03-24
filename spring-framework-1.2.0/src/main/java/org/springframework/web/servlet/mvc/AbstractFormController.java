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

package org.springframework.web.servlet.mvc;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>Form controller that autopopulates a form bean from the request.
 * This, either using a new bean instance per request, or using the same bean
 * when the <code>sessionForm</code> property has been set to <code>true</code>.</p>
 *
 * <p>This class is the base class for both framework subclasses like
 * {@link SimpleFormController SimpleFormController} and
 * {@link AbstractWizardFormController AbstractWizardFormController}, and
 * custom form controllers you can provide yourself.</p>
 *
 * <p>Both form-input views and after-submission views have to be provided
 * programmatically. To provide those views using configuration properties,
 * use the {@link SimpleFormController SimpleFormController}.</p>
 *
 * <p>Subclasses need to override <code>showForm</code> to prepare the form view,
 * and <code>processFormSubmission</code> to handle submit requests. For the latter,
 * binding errors like type mismatches will be reported via the given "errors" holder.
 * For additional custom form validation, a validator (property inherited from
 * BaseCommandController) can be used, reporting via the same "errors" instance.</p>
 *
 * <p>Comparing this Controller to the Struts notion of the <code>Action</code>
 * shows us that with Spring, you can use any ordinary JavaBeans or database-
 * backed JavaBeans without having to implement a framework-specific class
 * (like Struts' <code>ActionForm</code>). More complex properties of JavaBeans
 * (Dates, Locales, but also your own application-specific or compound types)
 * can be represented and submitted to the controller, by using the notion of
 * a <code>java.beans.PropertyEditor</code>. For more information on that
 * subject, see the workflow of this controller and the explanation of the
 * {@link BaseCommandController BaseCommandController}.</p>
 *
 * <p><b><a name="workflow">Workflow
 * (<a href="BaseCommandController.html#workflow">and that defined by superclass</a>):</b><br>
 * <ol>
 *  <li><b>The controller receives a request for a new form (typically a GET).</b></li>
 *  <li>Call to {@link #formBackingObject formBackingObject()} which by default,
 *      returns an instance of the commandClass that has been configured
 *      (see the properties the superclass exposes), but can also be overridden
 *      to e.g. retrieve an object from the database (that needs to be modified
 * using the form).</li>
 *  <li>Call to {@link #initBinder initBinder()} which allows you to register
 *      custom editors for certain fields (often properties of non-primitive
 *      or non-String types) of the command class. This will render appropriate
 *      Strings for those property values, e.g. locale-specific date strings.</li>
 *  <li>The {@link ServletRequestDataBinder ServletRequestDataBinder}
 *      gets applied to populate the new form object with initial request parameters.
 *      (<i>only if <code>bindOnNewForm</code> is set to <code>true</code></i>)</li>
 *  <li>Call to {@link #showForm(HttpServletRequest, HttpServletResponse, BindException) showForm()}
 *      to return a View that should be rendered (typically the view that renders
 *      the form). This method has to be implemented in subclasses.</li>
 *  <li>The showForm() implementation will call {@link #referenceData referenceData()},
 *      which you can implement to provide any relevant reference data you might need
 *      when editing a form (e.g. a List of Locale objects you're going to let the
 *      user select one from).</li>
 *  <li>Model gets exposed and view gets rendered, to let the user fill in the form.</li>
 *  <li><b>The controller receives a form submission (typically a POST).</b>
 *      To use a different way of detecting a form submission, override the
 *      {@link #isFormSubmission isFormSubmission} method.
 *      </li>
 *  <li>If <code>sessionForm</code> is not set, {@link #formBackingObject formBackingObject()}
 *      is called to retrieve a form object. Otherwise, the controller tries to
 *      find the command object which is already bound in the session. If it cannot
 *      find the object, it does a call to {@link #handleInvalidSubmit handleInvalidSubmit}
 *      which - by default - tries to create a new form object and resubmit the form.</li>
 *  <li>The {@link ServletRequestDataBinder ServletRequestDataBinder}
 *      gets applied to populate the form object with current request parameters.
 *  <li>Call to {@link #onBind onBind(HttpServletRequest, Object, Errors)} which allows
 *      you to do custom processing after binding but before validation (e.g. to manually
 *      bind request parameters to bean properties, to be seen by the Validator).</li>
 *  <li>If <code>validateOnBinding</code> is set, a registered Validator will be invoked.
 *      The Validator will check the form object properties, and register corresponding
 *      errors via the given {@link Errors Errors}</li> object.
 *  <li>Call to {@link #onBindAndValidate onBindAndValidate()} which allows you
 *      to do custom processing after binding and validation (e.g. to manually
 *      bind request parameters, and to validate them outside a Validator).</li>
 *  <li>Call {@link #processFormSubmission(HttpServletRequest, HttpServletResponse,
 *      Object, BindException) processFormSubmission()} to process the submission, with
 *      or without binding errors. This method has to be implemented in subclasses.</li>
 * </ol>
 * </p>
 *
 * <p>In session form mode, a submission without an existing form object in the
 * session is considered invalid, like in case of a resubmit/reload by the browser.
 * The {@link #handleInvalidSubmit handleInvalidSubmit} method is invoked then,
 * by default trying to resubmit. It can be overridden in subclasses to show
 * corresponding messages or to redirect to a new form, in order to avoid duplicate
 * submissions. The form object in the session can be considered a transaction
 * token in that case.</p>
 *
 * <p>Note that views should never retrieve form beans from the session but always
 * from the request, as prepared by the form controller. Remember that some view
 * technologies like Velocity cannot even access a HTTP session.</p>
 *
 * <p><b><a name="config">Exposed configuration properties</a>
 * (<a href="BaseCommandController.html#config">and those defined by superclass</a>):</b><br>
 * <table border="1">
 *  <tr>
 *      <td><b>name</b></td>
 *      <td><b>default</b></td>
 *      <td><b>description</b></td>
 *  </tr>
 *  <tr>
 *      <td>bindOnNewForm</td>
 *      <td>false</td>
 *      <td>Indicates whether to bind servlet request parameters when
 *          creating a new form. Otherwise, the parameters will only be
 *          bound on form submission attempts.</td>
 *  </tr>
 *  <tr>
 *      <td>sessionForm</td>
 *      <td>false</td>
 *      <td>Indicates whether the form object should be kept in the session
 *          when a user asks for a new form. This allows you e.g. to retrieve
 *          an object from the database, let the user edit it, and then persist
 *          it again. Otherwise, a new command object will be created for each
 *          request (even when showing the form again after validation errors).</td>
 *  </tr>
 * </table>
 * </p>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Alef Arendsen
 * @see #showForm(HttpServletRequest, HttpServletResponse, BindException)
 * @see #processFormSubmission
 * @see SimpleFormController
 * @see AbstractWizardFormController
 */
public abstract class AbstractFormController extends BaseCommandController {

	private boolean bindOnNewForm = false;

	private boolean sessionForm = false;


	/**
	 * Create a new AbstractFormController.
	 * <p>Subclasses should set the following properties, either in the constructor
	 * or via a BeanFactory: commandName, commandClass, bindOnNewForm, sessionForm.
	 * Note that commandClass doesn't need to be set when overriding
	 * <code>formBackingObject</code>, as the latter determines the class anyway.
	 * <p>"cacheSeconds" is by default set to 0 (-> no caching for all form controllers).
	 * @see #setCommandName
	 * @see #setCommandClass
	 * @see #setBindOnNewForm
	 * @see #setSessionForm
	 * @see #formBackingObject
	 */
	public AbstractFormController() {
		setCacheSeconds(0);
	}

	/**
	 * Set if request parameters should be bound to the form object
	 * in case of a non-submitting request, i.e. a new form.
	 */
	public final void setBindOnNewForm(boolean bindOnNewForm) {
		this.bindOnNewForm = bindOnNewForm;
	}

	/**
	 * Return if request parameters should be bound in case of a new form.
	 */
	public final boolean isBindOnNewForm() {
		return bindOnNewForm;
	}

	/**
	 * Activate resp. deactivate session form mode. In session form mode,
	 * the form is stored in the session to keep the form object instance
	 * between requests, instead of creating a new one on each request.
	 * <p>This is necessary for either wizard-style controllers that populate a
	 * single form object from multiple pages, or forms that populate a persistent
	 * object that needs to be identical to allow for tracking changes.
	 */
	public final void setSessionForm(boolean sessionForm) {
		this.sessionForm = sessionForm;
	}

	/**
	 * Return if session form mode is activated.
	 */
	public final boolean isSessionForm() {
		return sessionForm;
	}


	/**
	 * Handles two cases: form submissions and showing a new form.
	 * Delegates the decision between the two to isFormSubmission,
	 * always treating requests without existing form session attribute
	 * as new form when using session form mode.
	 */
	protected final ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// Form submission or new form to show?
		if (isFormSubmission(request)) {

			// Form submission: in session-form mode, we need to find
			// the form object in the HTTP session.
			if (isSessionForm()) {
				HttpSession session = request.getSession(false);
				if (session == null || session.getAttribute(getFormSessionAttributeName(request)) == null) {
					// Cannot submit a session form if no form object is in the session.
					return handleInvalidSubmit(request, response);
				}
			}

			// Found form object in HTTP session: fetch form object,
			// bind, validate, process submission.
			Object command = getCommand(request);
			ServletRequestDataBinder binder = bindAndValidate(request, command);
			return processFormSubmission(request, response, command, binder.getErrors());
		}

		else {
			// New form to show: render form view.
			return showNewForm(request, response);
		}
	}

	/**
	 * Determine if the given request represents a form submission.
	 * <p>Default implementation treats a POST request as form submission.
	 * Note: If the form session attribute doesn't exist when using session form
	 * mode, the request is always treated as new form by handleRequestInternal.
	 * <p>Subclasses can override this to use a custom strategy, e.g. a specific
	 * request parameter (assumably a hidden field or submit button name).
	 * @param request current HTTP request
	 * @return if the request represents a form submission
	 */
	protected boolean isFormSubmission(HttpServletRequest request) {
		return "POST".equals(request.getMethod());
	}

	/**
	 * Return the name of the HttpSession attribute that holds the form object
	 * for this form controller.
	 * <p>Default implementation delegates to the <code>getFormSessionAttributeName</code>
	 * version without arguments.
	 * @param request current HTTP request
	 * @return the name of the form session attribute, or null if not in session form mode
	 * @see #getFormSessionAttributeName
	 * @see HttpSession#getAttribute
	 */
	protected String getFormSessionAttributeName(HttpServletRequest request) {
		return getFormSessionAttributeName();
	}

	/**
	 * Return the name of the HttpSession attribute that holds the form object
	 * for this form controller.
	 * <p>Default is an internal name, of no relevance to applications, as the form
	 * session attribute is not usually accessed directly. Can be overridden to use
	 * an application-specific attribute name, which allows other code to access
	 * the session attribute directly.
	 * @return the name of the form session attribute
	 * @see HttpSession#getAttribute
	 */
	protected String getFormSessionAttributeName() {
		return getClass().getName() + ".FORM." + getCommandName();
	}

	/**
	 * Show a new form. Prepares a backing object for the current form
	 * and the given request, including checking its validity.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return the prepared form view
	 * @throws Exception in case of an invalid new form object
	 * @see #getErrorsForNewForm
	 */
	protected final ModelAndView showNewForm(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		logger.debug("Displaying new form");
		return showForm(request, response, getErrorsForNewForm(request));
	}

	/**
	 * Create a BindException instance for a new form.
	 * Called by <code>showNewForm</code>.
	 * <p>Can be used directly when intending to show a new form but with
	 * special errors registered on it (for example, on invalid submit).
	 * Usually, the resulting BindException will be passed to
	 * <code>showForm</code>, after registering the errors on it.
	 * @param request current HTTP request
	 * @return the BindException instance
	 * @throws Exception in case of an invalid new form object
	 * @see #showNewForm
	 * @see #showForm(HttpServletRequest, HttpServletResponse, BindException)
	 * @see #handleInvalidSubmit
	 */
	protected final BindException getErrorsForNewForm(HttpServletRequest request) throws Exception {
		// Create form-backing object for new form.
		Object command = formBackingObject(request);
		if (command == null) {
			throw new ServletException("Form object returned by formBackingObject() must not be null");
		}
		if (!checkCommand(command)) {
			throw new ServletException("Form object returned by formBackingObject() must match commandClass");
		}

		// Bind without validation, to allow for prepopulating a form, and for
		// convenient error evaluation in views (on both first attempt and resubmit).
		ServletRequestDataBinder binder = createBinder(request, command);
		if (isBindOnNewForm()) {
			logger.debug("Binding to new form");
			binder.bind(request);
		}

		// Return BindException object that resulted from binding.
		return binder.getErrors();
	}

	/**
	 * Return the form object for the given request.
	 * <p>Calls <code>formBackingObject</code> if not in session form mode.
	 * Else, retrieves the form object from the session. Note that the form object
	 * gets removed from the session, but it will be re-added when showing the
	 * form for resubmission.
	 * @param request current HTTP request
	 * @return object form to bind onto
	 * @throws Exception in case of invalid state or arguments
	 * @see #formBackingObject
	 */
	protected final Object getCommand(HttpServletRequest request) throws Exception {
		// If not in session-form mode, create a new form-backing object.
		if (!isSessionForm()) {
			return formBackingObject(request);
		}

		// Session-form mode: retrieve form object from HTTP session attribute.
		HttpSession session = request.getSession(false);
		if (session == null) {
			throw new ServletException("Must have session when trying to bind (in session-form mode)");
		}
		String formAttrName = getFormSessionAttributeName(request);
		Object sessionFormObject = session.getAttribute(formAttrName);
		if (sessionFormObject == null) {
			throw new ServletException("Form object not found in session (in session-form mode)");
		}

		// Remove form object from HTTP session: we might finish the form workflow
		// in this request. If it turns out that we need to show the form view again,
		// we'll re-bind the form object to the HTTP session.
		if (logger.isDebugEnabled()) {
			logger.debug("Removing form session attribute [" + formAttrName + "]");
		}
		session.removeAttribute(formAttrName);

		return sessionFormObject;
	}

	/**
	 * Retrieve a backing object for the current form from the given request.
	 * <p>The properties of the form object will correspond to the form field values
	 * in your form view. This object will be exposed in the model under the specified
	 * command name, to be accessed under that name in the view: for example, with
	 * a "spring:bind" tag. The default command name is "command".
	 * <p>Note that you need to activate session form mode to reuse the form-backing
	 * object across the entire form workflow. Else, a new instance of the command
	 * class will be created for each submission attempt, just using this backing
	 * object as template for the initial form.
	 * <p>Default implementation calls <code>BaseCommandController.createCommand</code>,
	 * creating a new empty instance of the command class.
	 * Subclasses can override this to provide a preinitialized backing object.
	 * @param request current HTTP request
	 * @return the backing object
	 * @throws Exception in case of invalid state or arguments
	 * @see #setCommandName
	 * @see #setCommandClass
	 * @see #createCommand
	 */
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return createCommand();
	}


	/**
	 * Prepare the form model and view, including reference and error data.
	 * Can show a configured form page, or generate a form view programmatically.
	 * <p>A typical implementation will call
	 * <code>showForm(request, errors, "myView")</code>
	 * to prepare the form view for a specific view name, returning the
	 * ModelAndView provided there.
	 * <p>For building a custom ModelAndView, call <code>errors.getModel()</code>
	 * to populate the ModelAndView model with the command and the Errors instance,
	 * under the specified command name, as expected by the "spring:bind" tag.
	 * You also need to include the model returned by <code>referenceData</code>.
	 * <p>Note: If you decide to have a "formView" property specifying the
	 * view name, consider using SimpleFormController.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param errors validation errors holder
	 * @return the prepared form view, or null if handled directly
	 * @throws Exception in case of invalid state or arguments
	 * @see #showForm(HttpServletRequest, BindException, String)
	 * @see Errors
	 * @see BindException#getModel
	 * @see #referenceData(HttpServletRequest, Object, Errors)
	 * @see SimpleFormController#setFormView
	 */
	protected abstract ModelAndView showForm(
			HttpServletRequest request, HttpServletResponse response, BindException errors)
			throws Exception;

	/**
	 * Prepare model and view for the given form, including reference and errors.
	 * <p>In session form mode: Re-puts the form object in the session when
	 * returning to the form, as it has been removed by getCommand.
	 * <p>Can be used in subclasses to redirect back to a specific form page.
	 * @param request current HTTP request
	 * @param errors validation errors holder
	 * @param viewName name of the form view
	 * @return the prepared form view
	 * @throws Exception in case of invalid state or arguments
	 */
	protected final ModelAndView showForm(HttpServletRequest request, BindException errors, String viewName)
			throws Exception {
		return showForm(request, errors, viewName, null);
	}

	/**
	 * Prepare model and view for the given form, including reference and errors,
	 * adding a controller-specific control model.
	 * <p>In session form mode: Re-puts the form object in the session when returning
	 * to the form, as it has been removed by getCommand.
	 * <p>Can be used in subclasses to redirect back to a specific form page.
	 * @param request current HTTP request
	 * @param errors validation errors holder
	 * @param viewName name of the form view
	 * @param controlModel model map containing controller-specific control data
	 * (e.g. current page in wizard-style controllers or special error message)
	 * @return the prepared form view
	 * @throws Exception in case of invalid state or arguments
	 */
	protected final ModelAndView showForm(
			HttpServletRequest request, BindException errors, String viewName, Map controlModel) throws Exception {

		// In session form mode, re-expose form object as HTTP session attribute.
		// Re-binding is necessary for proper state handling in a cluster,
		// to notify other nodes of changes in the form object.
		if (isSessionForm()) {
			String formAttrName = getFormSessionAttributeName(request);
			if (logger.isDebugEnabled()) {
				logger.debug("Setting form session attribute [" + formAttrName + "] to: " + errors.getTarget());
			}
			request.getSession().setAttribute(formAttrName, errors.getTarget());
		}

		// Fetch errors model as starting point, containing form object under
		// "commandName", and corresponding Errors instance under internal key.
		Map model = errors.getModel();

		// Merge reference data into model, if any.
		Map referenceData = referenceData(request, errors.getTarget(), errors);
		if (referenceData != null) {
			model.putAll(referenceData);
		}

		// Merge control attributes into model, if any.
		if (controlModel != null) {
			model.putAll(controlModel);
		}

		// Trigger rendering of the specified view, using the final model.
		return new ModelAndView(viewName, model);
	}

	/**
	 * Create a reference data map for the given request, consisting of
	 * bean name/bean instance pairs as expected by ModelAndView.
	 * <p>Default implementation returns null.
	 * Subclasses can override this to set reference data used in the view.
	 * @param request current HTTP request
	 * @param command form object with request parameters bound onto it
	 * @param errors validation errors holder
	 * @return a Map with reference data entries, or null if none
	 * @throws Exception in case of invalid state or arguments
	 * @see ModelAndView
	 */
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		return null;
	}


	/**
	 * Process form submission request. Called by <code>handleRequestInternal</code>
	 * in case of a form submission, with or without binding errors. Implementations
	 * need to proceed properly, typically showing a form view in case of binding
	 * errors or performing a submit action else.
	 * <p>Subclasses can implement this to provide custom submission handling
	 * like triggering a custom action. They can also provide custom validation
	 * and call <code>showForm</code> or proceed with the submission accordingly.
	 * <p>For a success view, call <code>errors.getModel()</code> to populate the
	 * ModelAndView model with the command and the Errors instance, under the
	 * specified command name, as expected by the "spring:bind" tag. For a form view,
	 * simply return the ModelAndView object provided by <code>showForm</code>.
	 * @param request current servlet request
	 * @param response current servlet response
	 * @param command form object with request parameters bound onto it
	 * @param errors holder without errors (subclass can add errors if it wants to)
	 * @return the prepared model and view, or null
	 * @throws Exception in case of errors
	 * @see #handleRequestInternal
	 * @see #isFormSubmission
	 * @see #showForm(HttpServletRequest, HttpServletResponse, BindException)
	 * @see Errors
	 * @see BindException#getModel
	 */
	protected abstract ModelAndView processFormSubmission(
			HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
			throws Exception;

	/**
	 * Handle an invalid submit request, e.g. when in session form mode but no form object
	 * was found in the session (like in case of an invalid resubmit by the browser).
	 * <p>Default implementation simply tries to resubmit the form with a new form object.
	 * This should also work if the user hit the back button, changed some form data,
	 * and resubmitted the form.
	 * <p>Note: To avoid duplicate submissions, you need to override this method.
	 * Either show some "invalid submit" message, or call <code>showNewForm</code> for
	 * resetting the form (prepopulating it with the current values if "bindOnNewForm"
	 * is true). In this case, the form object in the session serves as transaction token.
	 * <pre>
	 * protected ModelAndView handleInvalidSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {
	 *   return showNewForm(request, response);
	 * }</pre>
	 * You can also show a new form but with special errors registered on it:
	 * <pre>
	 * protected ModelAndView handleInvalidSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {
	 *   BindException errors = getErrorsForNewForm(request);
	 *   errors.reject("duplicateFormSubmission", "Duplicate form submission");
	 *   return showForm(request, response, errors);
	 * }</pre>
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a prepared view, or null if handled directly
	 * @throws Exception in case of errors
	 * @see #showNewForm
	 * @see #getErrorsForNewForm
	 * @see #showForm(HttpServletRequest, HttpServletResponse, BindException)
	 * @see #setBindOnNewForm
	 */
	protected ModelAndView handleInvalidSubmit(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Object command = formBackingObject(request);
		ServletRequestDataBinder binder = bindAndValidate(request, command);
		return processFormSubmission(request, response, command, binder.getErrors());
	}

}
