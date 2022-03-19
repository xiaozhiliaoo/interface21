package com.interface21.context.support;

import com.interface21.context.*;

/**
 *
 * @author  Rod Johnson
 * @since January 21, 2001
 */
public class ConsoleListener implements ApplicationListener {
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	//private WebApplicationManager webApplicationManager;
	
	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	
	public ConsoleListener() {
	}
	
	
	//---------------------------------------------------------------------
	// Implementation of WebApplicationListener
	//---------------------------------------------------------------------
	/**
	 * Ignore log events
	 */
	public void onApplicationEvent(ApplicationEvent e) {
//		if (!(e instanceof LogEvent))
			log(e.toString());
	}
	
	private void log(String s) {
		System.out.println(s);
	}
	
	//public void setWebApplicationManager(WebApplicationManager webApplicationManager) {
	//	this.webApplicationManager = webApplicationManager;
	//}
    
}	// class ConsoleListener
