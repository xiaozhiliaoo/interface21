package com.interface21.web.servlet.view;

import javax.servlet.http.HttpServletRequest;

/**
 * Can be added to models
 */
public class RequestInfo {
	private HttpServletRequest request;
	
	public RequestInfo(HttpServletRequest request) {
		this.request = request;
	}
	
	public String getCountry() {
		return request.getLocale().getCountry();
	}
	
	public String getLanguage() {
		return request.getLocale().getLanguage();
	}
}