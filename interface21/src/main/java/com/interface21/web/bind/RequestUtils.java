
package com.interface21.web.bind;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

public abstract class RequestUtils {

	/**
	 * Subclasses can invoke this to reject methods programmatically
	 * @param method to reject
	 */
	public static void rejectRequestMethod(HttpServletRequest request, String method) throws ServletException {
		//logger.info("Disallowed '" + method + "' request");
		if (request.getMethod().equals(method))
			throw new ServletException("This resource does not support request method '" + method + "'");
	}
	
	
	/**
	 * @throws WebRequestBindingException: subclass of ServletException, so it doesn't
	 * need to be caught
	 */
	public static int getRequiredIntParameter(HttpServletRequest request, String name) throws WebRequestBindingException {
		String s = request.getParameter(name);
		if (s == null)
			throw new WebRequestBindingException("Required int parameter '" + name + "' was not supplied");
		if ("".equals(s))
			throw new WebRequestBindingException("Required int parameter '" + name + "' contained no value");
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException ex) {
				throw new WebRequestBindingException("Required int parameter '" + name + "' value of '" + s + "' was not a valid number");
		}
	}
	
	
	/**
	 * Can pass a distinguished value to default to enable checks of whether it was supplied
	 */
	public static int getIntParameter(HttpServletRequest request, String name, int defaultVal) throws WebRequestBindingException {
		try {
			return getRequiredIntParameter(request, name);
		}
		catch (WebRequestBindingException ex) {
			return defaultVal;
		}
	}
	
	
	public static double getRequiredDoubleParameter(HttpServletRequest request, String name) throws WebRequestBindingException {
		String s = request.getParameter(name);
		if (s == null)
			throw new WebRequestBindingException("Required double parameter '" + name + "' was not supplied");
		if ("".equals(s))
			throw new WebRequestBindingException("Required double parameter '" + name + "' contained no value");
		try {
			return Double.parseDouble(s);
		}
		catch (NumberFormatException ex) {
				throw new WebRequestBindingException("Required double parameter '" + name + "' value of '" + s + "' was not a valid number");
		}
	}
	
	
	/**
	 * True is true or yes (no case) or 1
	 */
	public static boolean getRequiredBooleanParameter(HttpServletRequest request, String name) throws WebRequestBindingException {
		String s = request.getParameter(name);
		if (s == null)
			throw new WebRequestBindingException("Required boolean parameter '" + name + "' was not supplied");
		if ("".equals(s))
			throw new WebRequestBindingException("Required boolean parameter '" + name + "' contained no value");
		return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equals("1");
	}
	

}
