package servletapi;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class TestHttpResponse implements HttpServletResponse {
	
	private HashMap headers = new HashMap();
	
	private Locale locale;
	
	private int bufsize = 12096;
	
	private int status = HttpServletResponse.SC_OK;

	/** Creates new TestHttpResponse */
    public TestHttpResponse() {
    }

	public boolean containsHeader(String str) {
		return headers.get(str) != null;
	}
	
	public String encodeUrl(String str) {
		throw new UnsupportedOperationException("encodeUrl");
	}
	
	public void setHeader(String str, String str1) {
		headers.put(str, str1);
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public void flushBuffer() throws java.io.IOException {
	}
	
	public void addCookie(Cookie cookie) {
	}
	
	public void sendError(int param) throws java.io.IOException {
	}
	
	public int getBufferSize() {
		return bufsize;
	}
	
	public void addDateHeader(String str, long param) {
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	public void setBufferSize(int param) {
		this.bufsize = param;
	}
	
	public String encodeRedirectURL(String str) {
		throw new UnsupportedOperationException("encodeRedirectUrl");
	}
	
	public void setStatus(int param, String str) {
		this.status = param;
	}
	
	public java.io.PrintWriter getWriter() throws java.io.IOException {
		return new java.io.PrintWriter(System.out);
	}

	@Override
	public void setCharacterEncoding(String charset) {

	}

	public boolean isCommitted() {
		return false;
	}
	
	public String getCharacterEncoding() {
		throw new UnsupportedOperationException("getCharacterEncoding");
	}

	@Override
	public String getContentType() {
		return null;
	}

	public void setDateHeader(String str, long param) {
		headers.put(str, "" + param);
	}
	
	public ServletOutputStream getOutputStream() throws java.io.IOException {
		throw new UnsupportedOperationException("getOutputStream");
	}
	
	public void addIntHeader(String str, int param) {
	}
	
	public String encodeRedirectUrl(String str) {
		throw new UnsupportedOperationException("encodeRedirectUrl");
	}
	
	public void setIntHeader(String str, int param) {
	}
	
	public void setContentType(String str) {
	}
	
	public void setContentLength(int param) {
	}
	
	public String encodeURL(String str) {
		throw new UnsupportedOperationException("encodeUrl");
	}
	
	public void sendRedirect(String str) throws java.io.IOException {
		System.out.println(">>>>>>>>>>>>>>> request.sendRedirect to [" + str + "]");
	}
	
	public void reset() {
	}
	
	public void addHeader(String str, String str1) {
	}
	
	public void sendError(int param, String str) throws java.io.IOException {
		this.status = param;
	}
	
	public void setStatus(int param) {
		this.status = param;
	}
	
	public void resetBuffer() {
	}
	
	
	
	public int getStatusCode() {
		return status;
	}
	
}	// class TestHttpResponse
