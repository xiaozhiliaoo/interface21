
package com.interface21.util;

import java14.java.util.logging.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import java.util.*;

/**
 * REG EXPS AREN'T THREADSAFE
 * SWITH TPO ORO:
 * llok at examples code
 * 
 * will be efficient enough as there won't be that
 * many reg exps
 * mainly literals
 * 
 * Choose a concrete compiler: worked with Perl5
 * /shows/[0-9]+.html
 * 
 * compiler.compile(pattern)
 * PatternMatcher . matches(input, pattern)
 */
public class StringMap {
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	private HashMap literals = new HashMap();
	
	/* List of RegExp inner class */
	private List regexps = new LinkedList();
	
	private class RegExp {
		// Not threadsafe
		public final RE re;
		public final String match;
		public RegExp(RE re, String match) {
			this.re = re;
			this.match = match;
		}
	}

	/**
	 * Constructor for StringMap.
	 */
	public StringMap() {
		super();
	}
	
	public void setMappings(Properties props) throws RESyntaxException {
		if (!props.isEmpty()) {
			Iterator itr = props.keySet().iterator();
			while (itr.hasNext()) {
				String exp = (String) itr.next();
				String mapped = props.getProperty(exp);
				// Prepend with / if it's not present
				//if (!url.startsWith("/"))
				//	url = "/" + url;
				if (exp.indexOf(":") == -1) {
					literals.put(exp, mapped);
					System.out.println("Literal match for '" + exp + "'");
				}
				else {
					exp = exp.substring(1);
					RE re = new RE(exp);
					regexps.add(new RegExp(re, mapped));
					System.out.println("Regular expression match for '" + exp + "'");
				}
			}
		}
	}
	
	public String getMap(String candidate) {
		String match = (String) literals.get(candidate);
		if (match != null)
			return match;
		// Try all regular expressions
		for (int i = 0; i < regexps.size(); i++) {
			RegExp r = (RegExp) regexps.get(i);
			if (r.re.match(candidate)) {
				return r.match;
			}
			else {
				System.out.println("No match for '" + candidate + "' on re [" + r.re + "]");
			}
		}
		
		return null;
	}

}
