package com.interface21.context;

import java.util.EventListener;


/**
 * Interface to be implemented by event listeners.
 * @author  Rod Johnson
 * @version $RevisionId$
 */
public interface ApplicationListener extends EventListener {

	/**
	* Handle an application event
	* @param e event to respond to
	*/
    void onApplicationEvent(ApplicationEvent e);

}

