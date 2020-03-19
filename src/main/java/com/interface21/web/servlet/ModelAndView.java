package com.interface21.web.servlet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java14.java.util.logging.Logger;


/**
 * Class to represent a model and view returned by
 * an handler used by a ControllerServlet.
 * The View can take the form of a reference to a View
 * object, or a String view name, which will need
 * to be resolved by a ViewResolver object.
 * @author  Rod Johnson
 * @version $RevisionId$
 */
public class ModelAndView {

	/** Static for efficiency, as a lot of ModelAndView objects are created in web applications */
	private static Logger logger = Logger.getLogger(ModelAndView.class.getName());

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/** Model */
	private Map	model;

	/** View */
	private View	view;
	
	private String	viewname;


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/** Creates new ModelAndView
	 * @param View view to render this model
	 * @param model Map of model names (Strings) to
	 * models (Objects). Model entries may not be null.
	 */
    public ModelAndView(View view, Map model) {
		this.view = view;
		// Less efficient than simply copying reference?
		this.model = new HashMap(model);
    }
	
	  public ModelAndView(String viewname, Map model) {
		this.viewname = viewname;
		// Less efficient than simply copying reference?
		this.model = new HashMap(model);
    }


	/** Convenient constructor to take a single model
	 */
	public ModelAndView(String viewname, String modelname, Object model) {
		this(viewname);
		this.model.put(modelname, model);
    }
	
	
	/** Convenient constructor to take a single model
	 */
	public ModelAndView(View view, String modelname, Object model) {
		this(view);
		this.model.put(modelname, model);
    }

	/** Convenient constructor when there is no model data to expose
	 */
	public ModelAndView(View view) {
		this.view = view;
		this.model = new HashMap();
    }
	
	public ModelAndView(String viewname) {
		this.viewname = viewname;
		this.model = new HashMap();
    }


	//---------------------------------------------------------------------
	// Public methods
	//---------------------------------------------------------------------
	/**
	 * Add a model object
	 */
	public void addObject(String name, Object o) {
		this.model.put(name, o);
	}
	
	// USE POLYMORPHISM?
	public boolean isReference() {
		return viewname != null;
	}

	public View getView() {
		return view;
	}
	
	public String getViewname() {
		return viewname;
	}

	public Map getModel() {
		return model;
	}

	public String toString() {
		String s = "ModelAndView: ";
		s += isReference() ? "reference to view with name '" + viewname + "'" :
								"materialized View is " + view;
		s += "; Model=[" + model + "]";
		return s;
		
	}

}
