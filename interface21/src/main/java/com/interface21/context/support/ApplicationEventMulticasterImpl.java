package com.interface21.context.support;

import com.interface21.context.ApplicationEvent;
import com.interface21.context.ApplicationEventMulticaster;
import com.interface21.context.ApplicationListener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Concrete implementation of ApplicationEventMulticaster
 * Doesn't permit multiple instances of the same listener.
 * @author Rod Johnson
 * @version $RevisionId$
 * COULD USE COPY ON WRITE - LEA:137
 */
public class ApplicationEventMulticasterImpl implements ApplicationEventMulticaster {

    //---------------------------------------------------------------------
	// Instance data
    //---------------------------------------------------------------------
    private Set   eventListeners = new HashSet();


    //---------------------------------------------------------------------
    // Constructors
    //---------------------------------------------------------------------
    /** Creates new WebApplicationEventMulticasterImpl */
	public ApplicationEventMulticasterImpl() {
    }


    //---------------------------------------------------------------------
	// Implementation of WebApplicationEventMulticaster
	//---------------------------------------------------------------------
    // SYNCH!?
    public void addApplicationListener(ApplicationListener l) {
        eventListeners.add(l);
    }

    public void removeApplicationListener(ApplicationListener l) {
        eventListeners.remove(l);
    }

    // SYNCH?? multiple threads at once?
    // USE ONE SEPARATE thread for this!?

	// Really want to cast every time? 
	// REally doesn't get called often enough to worry
    public void onApplicationEvent(ApplicationEvent e) {
       Iterator i = eventListeners.iterator();
       while (i.hasNext()) {
           ApplicationListener l = (ApplicationListener) i.next();
           l.onApplicationEvent(e);
       }
    }

	public void removeAllListeners() {
		eventListeners.clear();
	}

}	// class ApplicationEventMulticasterImpl
