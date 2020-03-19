package com.interface21.context.support;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.interface21.context.MessageSource;
import com.interface21.context.NestingMessageSource;
import com.interface21.context.NoSuchMessageException;


/**
 *
 * @author Rod Johnson
 * @version $RevisionId$
 */
public abstract class AbstractNestingMessageSource implements NestingMessageSource {

	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	private MessageSource parent;
	
	
	
	// CACHING POINT? LOOK AT RESOURCE BUNDLE IMPL?
	
	// IT DOES CACHE< but there's synching on the cache

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/** Creates new ResourceBundleMessageSource */
    public AbstractNestingMessageSource() {
    }


	//---------------------------------------------------------------------
	// Implementation of MessageSource
	//---------------------------------------------------------------------

	/** Set the parent that will be used to try to resolve messages
	 * that this object can't resolve.
	 * @param parent parent MessageSource that will be used to try to resolve messages
	 * that this object can't resolve. May be null, in which case]
	 * no further resolution will be possible
	 */
	public final void setParent(MessageSource parent) {
		this.parent = parent;
	}
	
	/**
	 * Try to resolve the message. Treat as an error if the message can't
	 * be found.
	 * @param code code to lookup up, such as 'calculator.noRateSet'
	 * @param locale Locale in which to do lookup
	 * @return message
	 * @throws NoSuchMessageException not found in any locale
	 */
	public final String getMessage(String code, Locale locale) throws NoSuchMessageException {
		try {
			String mesg = resolve(code, locale);
			if (mesg == null) {
				if (parent != null)
					return parent.getMessage(code, locale);
				throw new NoSuchMessageException(code);
			}
			return mesg;
		}
		catch (Exception ex) {
			// OR RTE?
			throw new NoSuchMessageException(code);
			// TODO ** Log warning
		}
	}
	
	
	/** Subclasses should 
	 * RUNTIME!?
	 * @return null if not found
	 * @throws if error
	 */
	protected abstract String resolve(String code, Locale locale) throws Exception;
	
	
	/**
	 * Try to resolve the message.Return default message if no message
	 * was found
	 * @param code code to lookup up, such as 'calculator.noRateSet'
	 * @param locale Locale in which to do lookup
	 * @param defaultMessage String to return if the lookup fails
	 * @return a resolved message if the lookup is successful;
	 * otherwise return the default message passed as a parameter
	 */
	public final String getMessage(String code, Locale locale, String defaultMessage) {
		try {
			return getMessage(code, locale);
		}
		catch (NoSuchMessageException ex) {
			return defaultMessage;
		}
	}
	
	
}
