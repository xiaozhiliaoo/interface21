/**
 * Feel free to modify this code to meet your needs.
 * These packages are described in "Expert One-on-One
 * J2EE Design and Development" by Rod Johnson (Wrox, 2002).
 * 
 * For commercial support, contact rod.johnson@interface21.com.
 */

package com.interface21.util;

import java.util.*;

/**
 *
 * @author  Rod Johnson
 * @since 16 April 2001
 * @version $RevisionId: ResultSetHandler.java,v 1.1 2001/09/07 12:48:57 rod Exp $
 */
public abstract class StringUtils {
	
	/**
	 * Count the occurrences of the substring in string s
	 * @param s string to search in. Returns 0 if this is null
	 * @param sub string to search for. Return 0 if this is null.
	 */
	public static int countOccurrencesOf(String s, String sub) {
		if (s == null || sub == null || "".equals(sub))
			return 0;
		int count = 0, pos = 0, idx = 0;
		while ( (idx = s.indexOf(sub, pos)) != -1) {
			++count;
			pos = idx + sub.length();
			//System.out.println("Idx="  + idx + " Pos=" + pos);
		}
		return count;
	}
 
    /**
     * Replaces all occurences of a substring within a string with another string.
     * @param inString String to examine
     * @param oldPattern String to replace
     * @param newPattern String to insert
     * @return a String with the replacements
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        // Pick up error conditions
        if (inString == null)
            return null;
        if (oldPattern == null || newPattern == null)
            return inString;
        
        StringBuffer sbuf = new StringBuffer();      // Output StringBuffer we'll build up
        int pos = 0;                        // Our position in the old string
        int index = inString.indexOf(oldPattern); // The index of an occurrence we've found, or -1
        int patLen = oldPattern.length();
        while (index >= 0) {
            sbuf.append(inString.substring(pos, index));
            sbuf.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }
        sbuf.append(inString.substring(pos));     // Remember to append any characters to the right of a match
        return sbuf.toString();
    }  // replace
    
    
    /**
     * @param pattern pattern to delete all occurrences of
     */
    public static String delete(String inString, String pattern) {
    	return replace(inString, pattern, "");
    }
    
    /**
     * @param chars characters to delete e.g. az\n will delete as, zs and new lines
     */
    public static String deleteAny(String inString, String chars) {
    	if (inString == null || chars == null)
    		return inString;
   		StringBuffer out = new StringBuffer();
    	for (int i = 0; i < inString.length(); i++) {
    		char c = inString.charAt(i);
    		if (chars.indexOf(c) == -1) {
    			out.append(c);
    		}
    	}
    	return out.toString();
    }
    
	
   /** Take a String which is a delimited list and convert it to a String array
    * @param s String
    * @param delimiter delimiter. This will not be returned
    * @return an array of the tokens in the list
    */
    public static String[] delimitedListToStringArray(String s, String delimiter) {
    	if (s == null)
    		return new String[0];
   		if (delimiter == null)
   			return new String[] { s };
    	
    	/*
        StringTokenizer st = new StringTokenizer(s, delimiter);
        String[] tokens = new String[st.countTokens()];
        System.out.println("length is  " +tokens.length);
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = st.nextToken();
        }
        return tokens;
      */

        List l = new LinkedList();
        int delimCount = 0;
        int pos = 0;
        int delpos = 0;
       	while ((delpos = s.indexOf(delimiter, pos)) != -1) {
        	l.add(s.substring(pos, delpos));
        	pos = delpos + delimiter.length();
        }
        if (pos <= s.length()) {
        	// Add rest of String
        	l.add(s.substring(pos));
        }        	
          
        return (String[]) l.toArray(new String[l.size()]);

    }	// delimitedListToStringArray
    
   /** Convert a CSV list into an array of Strings
    * @param s CSV list
    * @return an array of Strings. Returns the empty array if
    * s is null.
    */
    public static String[] commaDelimitedListToStringArray(String s) {
        return delimitedListToStringArray(s, ",");
    }
    
   /** Convenience method to convert a CSV string list to a set. Note that
    * this will suppress duplicates.
    * @param s CSV String
    * @return a Set of String entries in the list
    */
    public static Set commaDelimitedListToSet(String s) {
        Set set = new TreeSet();
        String[] tokens = commaDelimitedListToStringArray(s);
        for (int i = 0; i < tokens.length; i++)
            set.add(tokens[i]);
        return set;
    }
    
    /*
    public int countSubstrings(String s, String sub) {
    	if (s == null || sub == null)
    		return 0;
   		int pos = 0;
    	for (int count = 0; pos < sub.length; i++	
    }
    */
    
   /** Convenience method to return a String array as a delimited (e.g. CSV)
    * String. Useful for toString() implementations
    * @param arr array to display. Elements may be of any type (toString() will be
    * called on each element).
    * @param delim delimiter to use (probably a ,)
    */
    public static String arrayToDelimitedString(Object[] arr, String delim) {
        if (arr == null)
            return "null";
        else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < arr.length; i++) {
                if (i > 0)
                    sb.append(delim);
                sb.append(arr[i]);
            }
            return sb.toString();
        }
    }    // arrayToDelimitedString
    
	
   /** Convenience method to return a Collection as a delimited (e.g. CSV)
    * String. Useful for toString() implementations
    * @param c Collection to display
    * @param delim delimiter to use (probably a ,)
    */
    public static String collectionToDelimitedString(Collection c, String delim) {
    	 if (c == null)
            return "null";
        return iteratorToDelimitedString(c.iterator(), delim);
    }    // collectionToDelimitedString
    
     
    /** 
     * Convenience method to return a Collection as a delimited (e.g. CSV)
    * String. Useful for toString() implementations
    * @param c Collection to display
    * @param delim delimiter to use (probably a ,)
    */
    public static String iteratorToDelimitedString(Iterator itr, String delim) {
        if (itr == null)
            return "null";
        else {
            StringBuffer sb = new StringBuffer();
            int i = 0;
            while (itr.hasNext()) {
                if (i++ > 0)
                    sb.append(delim);
                sb.append(itr.next());
            }
            return sb.toString();
        }
    }    // collectionToDelimitedString
    
}	// class StringUtils
