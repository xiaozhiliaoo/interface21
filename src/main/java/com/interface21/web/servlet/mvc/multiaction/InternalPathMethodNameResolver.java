package com.interface21.web.servlet.mvc.multiaction;

import javax.servlet.http.HttpServletRequest;

import com.interface21.util.StringUtils;

/**
* Maps /foo/bar/baz.html to foo_bar_baz
* Dsn't support wildcards
* @author Rod Johnson
 */
public class InternalPathMethodNameResolver implements MethodNameResolver {
	
	/**
	* @return a method name: never null
	*/
	public String getHandlerMethodName(HttpServletRequest request) {
		String servletPath = request.getServletPath();
		String name = servletPath.substring(servletPath.indexOf("/") + 1);
		name = StringUtils.replace(name, "/", "_");
		if (name.indexOf(".") != -1)
			name = name.substring(0, name.indexOf("."));
		//System.out.println("REquest handler method name is '" + name + "'");
		return name;
	}
}