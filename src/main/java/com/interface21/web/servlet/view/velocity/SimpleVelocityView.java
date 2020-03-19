package com.interface21.web.servlet.view.velocity;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java14.java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;

import com.interface21.web.servlet.view.AbstractView;



/**
 * View using Velocity template engine
 * @author  Rod Johnson	
 * @version $RevisionId$
 */
public class SimpleVelocityView extends AbstractView {
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	 * Velocity Template
	 */
	private Template velocityTemplate;
	
	/** Name of the Velocity template */
	private String 	templateName;
	
	

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/** Creates new VelocityView */
    public SimpleVelocityView() {
    }
	 
	
	
	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------
	/** Set the name of the wrapped Velocity template.
	 * This will cause the template to be loaded.
	 * @param templateName the name of the wrapped Velocity template,
	 * relative to the Velocity template root. For example,
	 * "/ic/interestResult.vm".
	 */
	public void setTemplateName(String templateName) throws ServletException {			    	
	    this.templateName = templateName;
		try {
			this.velocityTemplate = Velocity.getTemplate(templateName);	
		}
		catch (Exception ex) {
			String mesg = "Can't load Velocity template '" + templateName + "' " +
				"in view with name '" + getName() + "'; " +
				"A common cause of this problem is failing to run the VelocityConfigServlet with loadOnStartup=2 to configure Velocity before views are loaded. " +
				"also check that your war contains this file";
			logger.logp(Level.SEVERE, getClass().getName(), "setTemplateName", mesg, ex);
			throw new ServletException(mesg, ex);
		}
	}	// setTemplateName
	
	
	
	//---------------------------------------------------------------------
	// Implementation of View
	//---------------------------------------------------------------------
	/** Render the view given the model to output.
	 * @param model combined output Map, with dynamic values
	 * taking precedence over static attributes
	 * @param request HttpServetRequest
	 * @param response HttpServletResponse
	 * @throws IOException if there is an IO exception trying to obtain
	 * or render the view
	 * @throws ServletException if there is any other error
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws ServletException {
		
		if (this.velocityTemplate == null)
			throw new ServletException("VelocityView with name '" + getName() + "' is not configured: templateName must have been set");
		
		response.setContentType(getContentType());
		
		try {		
			// Create Velocity context : 
			Context context = new VelocityContext();
			
			// Expose model to the VelocityContext
			exposeModelsAsContextAttributes(model, context);
		
			// Merge
			// Speed of resposne.getWriter!?
			this.velocityTemplate.merge(context, response.getWriter());
			
			if (logger.isLoggable(Level.FINE))
				logger.fine("Merged OK with Velocity template '" + templateName + "' in VelocityView with name '" + getName() + "'");
			
		}
		catch (IOException ex) {
			String mesg = "Couldn't write to response trying to merge Velocity template with name '" + this.templateName + "' in VelocityView with name '" + getName() + "'";

			logger.logp(Level.SEVERE, getClass().getName(), "renderMergedOutputModel", mesg, ex);
			throw new ServletException(mesg, ex);
		}
		catch (ParseErrorException ex) {
			String mesg = "Velocity template with name '" + this.templateName + "' appears to be invalid in VelocityView with name '" + getName() + "'";
			logger.logp(Level.SEVERE, getClass().getName(), "renderMergedOutputModel", mesg, ex);
			throw new ServletException(mesg, ex);
		}
		catch (Exception ex) {
			String mesg = "Unknown error trying to merge Velocity template with name '" + this.templateName + "' in VelocityView with name '" + getName() + "'";
			logger.logp(Level.SEVERE, getClass().getName(), "renderMergedOutputModel", mesg, ex);
			throw new ServletException(mesg, ex);
		}
	}	// render
	
	
	/**
	 * Expose the models in the given map as Velocity context attributes. Names will be
	 * taken from the map. This method can be used by different view type.
	 * @param model Map of model data to expose
	 * @param cContext VelocityContext to add data to
	 */
	private void exposeModelsAsContextAttributes(Map model,  Context vContext) {
		if (model != null) {
			Set keys = model.keySet();
			Iterator itr = keys.iterator();
			while (itr.hasNext()) {
				String modelname = (String) itr.next();
				Object val = model.get(modelname);

				// NULL!?

				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Added model with name '" + modelname + "' and class " + val.getClass() + " to Velocity context in view with name '" + getName() + "'");
				}
				vContext.put(modelname, val);
			}
		}
		else {
			logger.fine("Model is null. Nothing to expose to Velocity context in view with name '" + getName() + "'");
		}
	}	// exposeModelsAsContextAttributes
	
}	// VelocityView
