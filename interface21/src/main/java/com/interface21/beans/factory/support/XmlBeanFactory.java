package com.interface21.beans.factory.support;
import com.interface21.beans.*;
import com.interface21.beans.factory.BeanDefinitionStoreException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import com.interface21.beans.factory.BeanDefinitionStoreException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EventListener;

/**
 * Doesn't need to be root element: parses all bean definitions
 * in XML file
 * Registers with superclass, and relies on superclass's implementation
 * of BeanFactory
 * EAGER INSTANTIATE SINGLETON ISSUE: CAN SET!?
 * @author  Rod Johnson
 * @since 15 April 2001
 * @version $RevisionId$
 */
public class XmlBeanFactory extends ListableBeanFactoryImpl {
	
	/**
	 * Value of a T/F attribute that represents true.
	 * Anything else represents false. Case seNsItive.
	 */
	private static final String TRUE_ATTRIBUTE_VALUE = "true";

	private static final String BEANS_ELEMENT = "beans";
    
    private static final String BEAN_ELEMENT = "bean";
    
    private static final String CLASS_ATTRIBUTE = "class";
    
    private static final String PARENT_ATTRIBUTE = "parent";
    
   	private static final  String NAME_ATTRIBUTE = "name";
    
    private static final String SINGLETON_ATTRIBUTE = "singleton";
    
    private static final String  DISTINGUISHED_VALUE_ATTRIBUTE = "distinguishedValue";
    
    private static final String  NULL_DISTINGUISHED_VALUE = "null";
    
    private static final String PROPERTY_ELEMENT = "property";
	
	/** Reference to another bean */
	private static final String BEANREF_ATTRIBUTE = "beanRef";
    
    private static final String BINDING_ERROR_ELEMENT = "bindingError";
    
    private static final String REJECTED_VALUE_ELEMENT = "rejectedValue";
    
    private static final String ERROR_MESSAGE_ELEMENT = "message";
    
	private static final String LISTENER_ELEMENT = "listener";

	private static final String CUSTOM_BEAN_DEFINITION_ATTRIBUTE = "definitionClass";
	
	private static final String CUSTOM_DEFINITION_PROPERTY_ELEMENT = "customDefinition";

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/** Creates new XMLBeanFactory */
	public XmlBeanFactory(String filename) throws BeansException {
		try {
			logger.info("Loading XmlBeanFactory from file '" + filename + "'");
			loadBeanDefinitions(new FileInputStream(filename));
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("Can't open file [" + filename + "]", ex);
		}
	}

	public XmlBeanFactory(InputStream is) throws BeansException {
		loadBeanDefinitions(is);
	}

	/** 
	 * Creates new XMLPrototypeFactory 
	 */
	public XmlBeanFactory(ServletContext servletContext, String url) throws BeansException {
		InputStream is = servletContext.getResourceAsStream(url);
		if (is == null)
			throw new BeanDefinitionStoreException("No web application resource found at [" + url + "]", null);
		loadBeanDefinitions(is);
	}

	/** Creates new XMLPrototypeFactory */
	public XmlBeanFactory(Document doc) throws BeansException {
		loadBeanDefinitions(doc);
	}

	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------
	/** 
	 * Load definitions from this input stream and close it
	 */
	private void loadBeanDefinitions(InputStream is) throws BeansException {
		try {
			logger.info("Loading XmlBeanFactory from InputStream [" + is + "]");
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(is);
			loadBeanDefinitions(doc);
		}
		catch (ParserConfigurationException ex) {
			throw new FatalBeanException("ParserConfiguration exception parsing XML", ex);
		}
		catch (SAXException ex) {
			throw new FatalBeanException("XML document is invalid", ex);
		}
		catch (IOException ex) {
			throw new FatalBeanException("IOException parsing XML document", ex);
		}
		finally {
			try {
				if (is != null)
					is.close();
			}
			catch (IOException ex) {
				throw new FatalBeanException("IOException closing stream for XML document", ex);
			}
		}
	} // loadDefinitions (InputStream)

	/**
	 * Load bean definitions from the given DOM document.
	 * All calls go through this.
	 */
	private void loadBeanDefinitions(Document doc) throws BeansException {
		Element root = doc.getDocumentElement();
		logger.fine("Loading bean definitions");
		NodeList nl = root.getElementsByTagName(BEAN_ELEMENT);
		logger.fine("Found " + nl.getLength() + " <" + BEAN_ELEMENT + "> elements defining beans");
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			loadBeanDefinition((Element) n);
		}
		
		// Ask superclass to eagerly instantiate singletons
		preInstantiateSingletons();
	} 	// loadBeanDefinitions (Document)

	/** 
	 * Parse an element definition: we know this is a BEAN element 
	 * */
	private void loadBeanDefinition(Element el) throws BeansException {

		String name = el.getAttribute(NAME_ATTRIBUTE);
		logger.fine("Loading bean definition with name '" + name + "'");

		String customBeanDefinitionClassname = null;
		if (el.hasAttribute(CUSTOM_BEAN_DEFINITION_ATTRIBUTE))
			customBeanDefinitionClassname = el.getAttribute(CUSTOM_BEAN_DEFINITION_ATTRIBUTE);
		if (customBeanDefinitionClassname != null)
			logger.info("Found custom bean definition classname '" + customBeanDefinitionClassname + "'");

		// Create BeanDefinition now: we'll build up PropertyValues later
		BeanDefinition beanDefinition;

		PropertyValues pvs = getPropertyValueSubElements(el);

		if (customBeanDefinitionClassname == null) {
			beanDefinition = parseDefaultBeanDefinition(el, name, pvs);
		}
		else {
			logger.info("Loading custom definition of class '" + customBeanDefinitionClassname + "'");
			beanDefinition = parseCustomBeanDefinition(el, name, customBeanDefinitionClassname, pvs);
		}

		// Add listeners if the bean definition supports them
		if (beanDefinition instanceof AbstractBeanDefinition) {
			parseListenerSubElements(el, (AbstractBeanDefinition) beanDefinition);
		}

		//System.out.println("XMLBeanFactory.loadDefinition: " + beanDefinition);
		registerBeanDefinition(name, beanDefinition);
	}


	/**
	 * Parse a standard bean definition
	 */
	private BeanDefinition parseDefaultBeanDefinition(Element el, String beanName, PropertyValues pvs) {
		String classname = null;
		boolean singleton = true;
		if (el.hasAttribute(SINGLETON_ATTRIBUTE)) {
			// Default is singleton
			// Can override by making non-singleton if desired
			singleton = TRUE_ATTRIBUTE_VALUE.equals(el.getAttribute(SINGLETON_ATTRIBUTE));
		}
		try {
			if (el.hasAttribute(CLASS_ATTRIBUTE))
				classname = el.getAttribute(CLASS_ATTRIBUTE);
			String parent = null;
			if (el.hasAttribute(PARENT_ATTRIBUTE))
				parent = el.getAttribute(PARENT_ATTRIBUTE);
			if (classname == null && parent == null)
				throw new FatalBeanException("No classname or parent in bean definition [" + beanName + "]", null);
			return (classname != null)
				? (AbstractBeanDefinition) new DefaultRootBeanDefinition(Class.forName(classname), pvs, singleton)
				: new ChildBeanDefinitionImpl(parent, pvs, singleton);
		}
		catch (ClassNotFoundException ex) {
			throw new FatalBeanException("Error creating bean with name [" + beanName + "]: class '" + classname + "' not found", ex);
		}
	}	// parseDefaultBeanDefinition
	

	private BeanDefinition parseCustomBeanDefinition(Element el, String beanName, String customBeanDefinitionClass, PropertyValues pvs) {
		BeanWrapper customDefinitionBw = null;
		try {
			
			// COULD USE CONTEXT CLASS LOADER AS FALLBACK
			// in common impl
			customDefinitionBw = new BeanWrapperImpl(Class.forName(customBeanDefinitionClass));
		}
		catch (ClassNotFoundException ex) {
			throw new FatalBeanException("Error creating bean with name [" + beanName + "]: custom bean definition class '" + customBeanDefinitionClass + "' not found", ex);
		}
		
		if (!(customDefinitionBw.getWrappedInstance() instanceof BeanDefinition))
			throw new FatalBeanException("Error creating bean with name [" + beanName + "]: custom bean definition class '" + customBeanDefinitionClass + "' does not implement BeanDefinition", null);
			
		try {
			// Apply properties to custom bean definition
			PropertyValues customDefinitionPvs = new XmlSubelementPropertyValues(el, CUSTOM_DEFINITION_PROPERTY_ELEMENT);
			customDefinitionBw.setPropertyValues(customDefinitionPvs);
			
			// Custom definition must expose setPropertyValues
			customDefinitionBw.setPropertyValue("propertyValues", pvs);
			return (BeanDefinition) customDefinitionBw.getWrappedInstance();
		}
		catch (Exception ex) {
			throw new FatalBeanException("Error creating bean with name [" + beanName + "]", ex);
		}
	}	// parseCustomBeanDefinition
	

	/**
	 * Parse property value subelements of this bean element
	 */
	private PropertyValues getPropertyValueSubElements(Element beanEle) {
		String beanName = beanEle.getAttribute(NAME_ATTRIBUTE);
		NodeList nl = beanEle.getElementsByTagName(PROPERTY_ELEMENT);
		MutablePropertyValues pvs = new MutablePropertyValues();
		for (int i = 0; i < nl.getLength(); i++) {
			Element propEle = (Element) nl.item(i);
			//System.out.println("SEEN TAG [" + e.getTagName() + "]");
			parsePropertyElement(pvs, beanName, propEle);
		}
		return pvs;
	}

	/**
	 * Parse property value subelements of this bean element
	 */
	private void parseListenerSubElements(Element beanEle, AbstractBeanDefinition beanDefinition) {
		String beanName = beanEle.getAttribute(NAME_ATTRIBUTE);
		NodeList nl = beanEle.getElementsByTagName(LISTENER_ELEMENT);
		MutablePropertyValues pvs = new MutablePropertyValues();
		for (int i = 0; i < nl.getLength(); i++) {
			Element listenerEle = (Element) nl.item(i);
			//System.out.println("SEEN TAG [" + e.getTagName() + "]");
			parseListenerElement(beanName, beanDefinition, listenerEle);
		}
	}

	/** 
	 * Parse a property element 
	 */
	private void parsePropertyElement(MutablePropertyValues pvs, String name, Element e) throws DOMException {
		String propertyName = e.getAttribute(NAME_ATTRIBUTE);
		boolean isBeanRef = TRUE_ATTRIBUTE_VALUE.equals(e.getAttribute(BEANREF_ATTRIBUTE));
		Object val = null;
		String distinguishedValue = e.getAttribute(DISTINGUISHED_VALUE_ATTRIBUTE);
		if (distinguishedValue != null && distinguishedValue.equals(NULL_DISTINGUISHED_VALUE)) {
			val = null;
		}
		else {
			// Get text child: element must contain value
			e.normalize();
			NodeList nl2 = e.getChildNodes();
			if (nl2.getLength() != 1 || !(nl2.item(0) instanceof Text))
				throw new FatalBeanException("Unexpected element or type mismatch: " + nl2.item(0), null);
			Text t = (Text) nl2.item(0);
			// This will be a String
			val = t.getData();
			if (isBeanRef) {
				// It's a reference to another bean in this factory
				val = new RuntimeBeanReference(t.getData());
				logger.fine("Bean [" + name + "] references bean [" + t.getData() + "]");
			}
		}
		pvs.addPropertyValue(new PropertyValue(propertyName, val));
	}

	/** 
	 * Parse a listener element
	 */
	private void parseListenerElement(String name, AbstractBeanDefinition beanDefinition, Element e)
		throws BeansException {
		// Syntax is <listener [property="age"] beanRef="listenerRef" />
		// May be null
		String propertyName = e.getAttribute(PROPERTY_ELEMENT);
		String beanRef = e.getAttribute(BEANREF_ATTRIBUTE);
		if (beanRef == null)
			throw new FatalBeanException(
				"beanRef attribute is required for listener element for bean definition [" + name + "]",
				null);
		// Forward references!?

		// Singleton get
		//
		Object o = getBean(beanRef);
		if (o == null)
			throw new FatalBeanException(
				"Cannot resolve listener reference [" + beanRef + "] for bean definition [" + name + "]",
				null);
		if (!(o instanceof EventListener))
			throw new FatalBeanException("Listener reference [" + beanRef + "] for bean definition ["
					+ name
					+ "] must be a subclass of EventListener. ["
					+ o
					+ "] is not.",
				null);
		logger.fine("Bean [" + name + "] has listener[" + beanRef + "] for property [" + propertyName + "]");
		beanDefinition.addListener(propertyName, (EventListener) o);
	} // parseListener

} // class XmlBeanFactory