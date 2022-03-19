package com.interface21.web.servlet.view.webmacro;

import com.interface21.web.servlet.view.AbstractView;
import java14.java.util.logging.Level;
import org.webmacro.*;
import org.webmacro.servlet.WebContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;



/**
 * View using Velocity template engine
 * @author  Rod Johnson	
 * @version $RevisionId$
 */
public class WebMacroView extends AbstractView {
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	 * Velocity Template
	 */
	private Template wmTemplate;
	
	/** Name of the Velocity template */
	private String 	templateName;
	
	private WebMacro wm;
	
	/** Prototype Context */
	private WebContext	webContext;
	
	

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/** Creates new VelocityView */
    public WebMacroView() throws InitException {
    	this.wm = new WM();
    	Broker broker = Broker.getBroker();
    	this.webContext = new WebContext(broker);
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
			this.wmTemplate = this.wm.getTemplate(templateName);
		}
		catch (Exception ex) {
			String mesg = "Can't load WebMacro template '" + templateName + "' " +
				"in view with name '" + getName() + "';";
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
		
		if (this.wmTemplate == null)
			throw new ServletException("WebMacroView with name '" + getName() + "' is not configured: templateName must have been set");
		
		//response.setContentType(getContentType());
		
		FastWriter fw = null;
		
		try {		
			// Create Velocity context : 
			Context context = webContext.newInstance(request, response);
			
			// Expose model to the VelocityContext
			exposeModelsAsContextAttributes(model, context);
			
			 Locale locale = (Locale) wmTemplate.getParam (WMConstants.TEMPLATE_LOCALE);
            
                if (locale != null) {
                    response.setLocale(locale);
                }
                
                String encoding = (String) wmTemplate.getParam (WMConstants.TEMPLATE_OUTPUT_ENCODING);
                if (encoding == null) {
                    encoding = response.getCharacterEncoding ();
                }
                
                fw = FastWriter.getInstance (webContext.getBroker(), encoding);
                
                wmTemplate.write (fw, context);
                
                // now write the FW buffer to the response output stream
                writeFastWriter(response, fw);
			
			if (logger.isLoggable(Level.FINE))
				logger.fine("Merged OK with Velocity template '" + templateName + "' in VelocityView with name '" + getName() + "'");
		}
		catch (Exception ex) {
			String mesg = "Unknown error trying to merge Velocity template with name '" + this.templateName + "' in VelocityView with name '" + getName() + "'";
			logger.logp(Level.SEVERE, getClass().getName(), "renderMergedOutputModel", mesg, ex);
			throw new ServletException(mesg, ex);
		}
		finally {
           try {
              if (fw != null) {
                 fw.flush ();
                 fw.close ();
                 fw = null;
              }
           } catch (Exception e3) {
              // ignore disconnect
           }
        }
	}	// render
	
	/**
     * Helper method to write out a FastWriter (that has bufferd
     * the response) to a ServletResponse. This method will try to use
     * the response's OutputStream first and if this fails, fall back
     * to its Writer.
     * @param response where to write fast writer to
     * @param fw FastWriter, that has response buffered
     */
	// STOLEN: check license
    private void writeFastWriter(HttpServletResponse response,FastWriter fw) 
        throws IOException {
        OutputStream out;
        // We'll check, if the OutputStream is available
        try {
            out = response.getOutputStream();
        } catch (IllegalStateException e) {
            // Here comes a quick hack, we need a cleaner
            // solution in a future release. (skanthak)
                    
            // this means, that the ServletOutputStream is
            // not available, because the Writer has already
            // be used. We have to use it, although its
            // much slower, especially, because we need to
            // revert the encoding process now
            out = null;
            logger.warning("Using Writer instead of OutputStream");
        }
        if (out != null) {
            fw.writeTo(out);
        } else {
            response.getWriter().write(fw.toString());
        }
    }
    
	
	/**
	 * Expose the models in the given map as Velocity context attributes. Names will be
	 * taken from the map. This method can be used by different view type.
	 * @param model Map of model data to expose
	 * @param wmContext WebMacroContext to add data to
	 */
	private void exposeModelsAsContextAttributes(Map model,  Context wmContext) {
		if (model != null) {
			Set keys = model.keySet();
			Iterator itr = keys.iterator();
			while (itr.hasNext()) {
				String modelname = (String) itr.next();
				Object val = model.get(modelname);

				// NULL!?

				if (logger.isLoggable(Level.FINE))
					logger.fine("Added model with name '" + modelname + "' and class " + val.getClass() + " to Velocity context in view with name '" + getName() + "'");
				wmContext.put(modelname, val);
			}
		}
		else {
			logger.fine("Model is null. Nothing to expose to Velocity context in view with name '" + getName() + "'");
		}
	}	// exposeModelsAsContextAttributes
	
}	// VelocityView
