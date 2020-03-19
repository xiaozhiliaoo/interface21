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
public class ResourceBundleMessageSource extends AbstractNestingMessageSource {

	private String basename;
	
	

	public void setBasename(String basename)  {
		this.basename = basename;
		//bundle = ResourceBundle.getBundle(basenamez);
	}

	// CACHING POINT? LOOK AT RESOURCE BUNDLE IMPL?

	// IT DOES CACHE< but there's synching on the cache

	/** Creates new ResourceBundleMessageSource */
	public ResourceBundleMessageSource() {
	}

	/** Subclasses should 
	 * RUNTIME!?
	 * @return null if not found
	 * @throws if error
	 */
	protected String resolve(String code, Locale locale) throws Exception {
		 ResourceBundle bundle  = ResourceBundle.getBundle(basename, locale);
		return bundle.getString(code);
	}
	/** Show the state of this object
	 */
	public String toString() {
		return getClass().getName() + ": basename='" + basename + "'";
	}

}