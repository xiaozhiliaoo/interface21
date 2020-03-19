package com.interface21.web.context;

import com.interface21.context.ApplicationEvent;


/**
 *
 * @author  Rod Johnson
 * @since January 17, 2001
 * @version $RevisionId$
 */
public class RequestHandledEvent extends ApplicationEvent {

	/** CVS version identifier */
	public static final String REVISION_ID = "$Id: RequestHandledEvent.java,v 1.1.1.1 2002/08/01 12:52:04 Rod Johnson Exp $";

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
    private String  url;
    private long    timeMillis;
    private String  ip;
	
	/** Usually GET or POST */
	private String	method;
	
	/** Name of the servlet that handled this request is available */
	private String servletName;
	
    private Throwable   failureCause;
	

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
    /** Creates a new PageViewEvent */
	public RequestHandledEvent(Object source, String url, long timeMillis, String ip, String method, String servletName) {
        super(source);
        this.url = url;
        this.timeMillis = timeMillis;
        this.ip = ip;
		this.method = method;
		this.servletName = servletName;
	}
    
    public RequestHandledEvent(Object source, String url, long timeMillis, String ip, String method, String servletName, Throwable t) {
       this(source, url, timeMillis, ip, method, servletName);
       failureCause = t;
	}
    
    // TIME TAKEN FOR ITHGER EV£ENT - separatoe????

	//---------------------------------------------------------------------
	// Methods from XXXX interface
	//---------------------------------------------------------------------
    public String getURL() {
        return url;
    }
    
    public long getTimeMillis() {
        return timeMillis;
    }
    
    public String getIP() {
        return ip;
    }
    
    public boolean wasFailure() {
        return failureCause != null;
    }
    
    public Throwable getFailureCause() {
        return failureCause;
    }
	
	public String getMethod() {
		return method;
	}
	
	public String getServletName() {
		return servletName;
	}
    
    public String toString() {
        StringBuffer sb = new StringBuffer("RequestHandledEvent: url=[" + getURL() + "] time=" + getTimeMillis() + "ms");
		sb.append(" ip=" + getIP() + " method='" + getMethod() + "' servletName='" + getServletName() + "'");
		sb.append(" client=" + getIP() + " ");
		sb.append(" status=");
        sb.append(!wasFailure() ? "OK" : "failed: "+ getFailureCause());
        return sb.toString();
    }

}	// class RequestHandledEvent
