
package com.interface21.web.servlet.view.xslt;


import org.apache.xml.utils.WrappedRuntimeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Xalan extension examples.
 * @author Taylor Cowan
 * @version 1.0
 */
public class FormatHelper {


    /**
	 * Creates a formatted-date node with the given
	 * ISO language and country strings.
     */
    public static Node dateTimeElement(long date, String language, String country) {
        Locale l = new Locale(language, country);  
        return  dateTimeElement(date, l);
     }
    
    public static Node dateTimeElement (long time) {
    	return dateTimeElement(time, Locale.getDefault());
    }
    
    
    // NEED TIME ALSO:
    
    /**
     * Can make locale automatic!?
     */
    public static Node dateTimeElement (long time, Locale locale) {
    	
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element dateNode = doc.createElement("formatted-date");
            
            // Works in most locales
            SimpleDateFormat df = (SimpleDateFormat)DateFormat.
				getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
            
            Date d = new Date(time);
            df.applyPattern("MMMM"); 
            addChild(dateNode, "month", df.format(d));
            df.applyPattern("EEEE"); 
            addChild(dateNode, "day-of-week", df.format(d));
            df.applyPattern("yyyy"); 
            addChild(dateNode,"year", df.format(d));
			df.applyPattern("dd"); 
			addChild(dateNode, "day-of-month", df.format(d));
			df.applyPattern("h"); 
			addChild(dateNode, "hours", df.format(d));
			df.applyPattern("mm"); 
			addChild(dateNode, "minutes", df.format(d));
			df.applyPattern("a"); 
			addChild(dateNode, "am-pm", df.format(d));
            return  dateNode;
        } catch (Exception ex) {throw  new WrappedRuntimeException(ex);}
    }
    
    public static String currency(double amount, Locale locale) {
    	
    	NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
    	return nf.format(amount);
    }
    
    public static String currency(double amount, String language, String country) {
    	Locale locale = null;
    	if (language == null || country == null) {
    		locale = Locale.getDefault();
    	}
    	else {
        	 locale = new Locale(language, country);  
    	}
    	return currency(amount, locale);
    }

    /**
     * utility method for adding text nodes.
     */
    private static void addChild (Node parent, String name, String text) {
        Element child = parent.getOwnerDocument().createElement(name);
        child.appendChild(parent.getOwnerDocument().createTextNode(text));
        parent.appendChild(child);
    }
}




