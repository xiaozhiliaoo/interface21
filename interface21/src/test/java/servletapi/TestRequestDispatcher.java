package servletapi;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class TestRequestDispatcher implements RequestDispatcher {
	
	private String url;

	/** Creates new TestRequestDispatcher */
    public TestRequestDispatcher(String url) {
		this.url = url;
    }

	public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, java.io.IOException {
		System.out.println("RequestDispatcher -- FORWARDING to [" + url + "]");
	}
	
	public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, java.io.IOException {
		System.out.println("RequestDispatcher -- INCLUDING [" + url + "]");
	}
	
}	// class TestRequestDispatcher
