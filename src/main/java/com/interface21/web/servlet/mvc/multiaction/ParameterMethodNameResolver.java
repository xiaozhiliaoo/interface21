package com.interface21.web.servlet.mvc.multiaction;

import javax.servlet.http.HttpServletRequest;

import com.interface21.util.StringUtils;

/**
 * Looks for action parameter value
*/
public class ParameterMethodNameResolver implements MethodNameResolver {
	
	private String paramName = "action";
	
	public void setParamName(String paramName) {
		this.paramName = paramName;
	} 
	
	
	/**
	* @return a method name: never null
	*/
	public String getHandlerMethodName(HttpServletRequest request) throws NoSuchRequestHandlingMethodException {
		String name = request.getParameter(paramName);
		if (name == null)
			throw new NoSuchRequestHandlingMethodException(request);
		return name;
	}
}