package com.interface21.context;

import java.util.Locale;

/**
 * Interface to be implemented by objects that can resolve messages
 * @author  Rod Johnson
 * @version $RevisionId$
 */
public interface MessageSource {		
	
	/**
	 * Try to resolve the message.Return default message if no message
	 * was found
	 * @param code code to lookup up, such as 'calculator.noRateSet'
	 * @param locale Locale in which to do lookup
	 * @param defaultMessage String to return if the lookup fails
	 * @return a resolved message if the lookup is successful;
	 * otherwise return the default message passed as a parameter
	 */
	String getMessage(String code, Locale locale, String defaultMessage);
	
	/**
	 * Try to resolve the message. Treat as an error if the message can't
	 * be found.
	 * @param code code to lookup up, such as 'calculator.noRateSet'
	 * @param locale Locale in which to do lookup
	 * @return message
	 * @throws NoSuchMessageException not found in any locale
	 */
	String getMessage(String code, Locale locale) throws NoSuchMessageException;

}

