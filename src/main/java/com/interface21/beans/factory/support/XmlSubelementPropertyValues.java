
package com.interface21.beans.factory.support;

import java14.java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.interface21.beans.FatalBeanException;
import com.interface21.beans.MutablePropertyValues;
import com.interface21.beans.PropertyValue;
import com.interface21.beans.PropertyValues;

/**
 * PropertyValues implementation created from ServetConfig parameters.
 * <br/>This class is immutable once initialized.
 * @author Rod Johnson
 * @version $RevisionId$
 */
class XmlSubelementPropertyValues implements PropertyValues {
	
	private static Logger logger = Logger.getLogger(XmlSubelementPropertyValues.class.getName());
	
	/** PropertyValues delegate. We use delegation rather than simply subclass
	 * MutablePropertyValues as we don't want to expose MutablePropertyValues's
	 * update methods. This class is immutable once initialized.
	 */
	private MutablePropertyValues mutablePropertyValues;		

	/** Creates new PropertyValues object
	 * @param config ServletConfig we'll use to take PropertyValues from
	 * @throws ServletException should never be thrown from this method
	 */
	// IGNORE PARAM?
    public XmlSubelementPropertyValues(Element e, String match) throws Exception {
    	logger.info("Looking for property value subelements with tag name <" + match + ">");
		mutablePropertyValues = new MutablePropertyValues();
		NodeList nl = e.getElementsByTagName(match);
		for (int i = 0; i < nl.getLength(); i++) {
			Element propEle = (Element) nl.item(i);
			String propName = propEle.getAttribute("property");
			if ("".equals(propName))
				throw new Exception("'property' attribute required in element <" + propEle.getTagName() + ">");
			NodeList nl2 = propEle.getChildNodes();
			if (nl2.getLength() != 1 || !(nl2.item(0) instanceof Text))
				throw new FatalBeanException("Unexpected element or type mismatch: " + nl2.item(0), null);
			Text t = (Text) nl2.item(0);
			// This will be a String
			String propVal = t.getData();
			PropertyValue pv = new PropertyValue(propName, propVal);
			logger.info("Found new XML subelement property value [" + pv + "]");
			mutablePropertyValues.addPropertyValue(pv);
		}
		logger.info("Found property values: [" + mutablePropertyValues + "]");
	}
	
	
	/** Return an array of the PropertyValue objects
	 * held in this object.
	 * @return an array of the PropertyValue objects
	 * held in this object.
 	*/
	public PropertyValue[] getPropertyValues() {
		// We simply let the delegate handle this
		return mutablePropertyValues.getPropertyValues();
	}
	
	/** Is there a propertyValue object for this property?
	 * @param propertyName name of the property we're interested in
	 * @return whether there is a propertyValue object for this property?
	 */
	public boolean contains(String propertyName) {
		return mutablePropertyValues.contains(propertyName);
	}
	
	public PropertyValue getPropertyValue(String propertyName) {
		return mutablePropertyValues.getPropertyValue(propertyName);
	}
	
}
