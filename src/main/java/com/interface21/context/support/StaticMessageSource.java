package com.interface21.context.support;

import java.util.HashMap;
import java.util.Locale;

public class StaticMessageSource extends AbstractNestingMessageSource {
	
	private HashMap	messages = new HashMap();
	

	/**
	 * @see MessageSource#getMessage(String, Locale, String)
	 */
	protected String resolve(String code, Locale locale) {
		return (String) messages.get(code);
	}
	
	public void addMessage(String code, String message) {
		messages.put(code, message);
	}

}

