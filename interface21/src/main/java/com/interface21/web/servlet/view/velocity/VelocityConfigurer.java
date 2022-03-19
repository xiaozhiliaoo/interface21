package com.interface21.web.servlet.view.velocity;

import com.interface21.context.ApplicationContextException;
import com.interface21.web.context.support.WebApplicationObjectSupport;
import com.interface21.web.util.WebUtils;
import java14.java.util.logging.Logger;
import org.apache.velocity.app.Velocity;

import java.io.IOException;
import java.util.Properties;

public class VelocityConfigurer extends WebApplicationObjectSupport {

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());

	public static final String DEFAULT_VELOCITY_PROPERTIES = "velocity.properties";

	private String url = "/WEB-INF/velocity.properties";

	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 *  Initializes the Velocity runtime, first calling 
	 *  loadConfiguration(ServletConvig) to get a 
	 *  java.util.Properties of configuration information
	 *  and then calling Velocity.init().  Override this
	 *  to do anything to the environment before the 
	 *  initialization of the singelton takes place, or to 
	 *  initialize the singleton in other ways.
	 */
	protected void init() throws ApplicationContextException {
		try {
			Properties p = new Properties();
			logger.config("Loading Velocity properties from [" + this.url + "]");
			p.load(WebUtils.getResourceInputStream(this.url, getWebApplicationContext().getServletContext()));

			Velocity.init(p);
		}
		// TODO: 2017/8/25 编译报错 注释了
		/*catch (ServletException e) {
			throw new ApplicationContextException("Error loading Velocity config from [" + this.url + "]", e);
		}*/
		catch (IOException e) {
			throw new ApplicationContextException("Error loading Velocity config from [" + this.url + "]", e);
		}
		catch (Exception e) {
			throw new ApplicationContextException(
				"Error initializing Velocity from properties file (loaded OK) @[" + this.url + "]",
				e);
		}
	}

}