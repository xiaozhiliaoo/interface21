package com.interface21.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * NB: must be registered
 * Is registered by BeanWrapperImpl
 * Format is defined in java.util.Properties documentation.
 * Each property must be on a new line.
 */
public class PropertiesEditor extends PropertyEditorSupport {
	/**
	 * @see PropertyEditor#setAsText(String)
	 */
	public void setAsText(String s) throws IllegalArgumentException {
		Properties props = new Properties();

		//System.out.println("String is [" + s + "]");

		// Zap whitespace
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			System.out.println("Tok=[" + tok + "]");

			// Tokens look like "/welcome.html=mainController"
			int eqpos = tok.indexOf("=");
			String url = tok.substring(0, eqpos);
			String beanName = tok.substring(eqpos + 1);
			props.put(url, beanName);
		}


		// NB: the following code, using properties default works in JBoss,
		// but not Orion
//		try {
//			props.load(new ByteArrayInputStream(s.getBytes()));
//		}
//		catch (IOException ex) {
//			// Shouldn't happen
//			throw new IllegalArgumentException("Failed to read String");
//		}

		setValue(props);
	}

}

