/*
 * $Id: TypedElementAdapter.java,v 1.1.1.1 2002/08/01 12:52:05 Rod Johnson Exp $
 * $Source: c:\\work\\cvs/framework/src/org/infohazard/domify2/TypedElementAdapter.java,v $
 */

package org.infohazard.domify2;

import org.apache.log4j.Category;
import org.infohazard.domify2.TypedElementAdapter.TypeNamedNodeMapAdapter;
import org.w3c.dom.*;
//import org.w3c.dom.*;

/**
 */
public class TypedElementAdapter extends ElementAdapter
{
	/** Logging category for log4j. */
	protected static Category log = Category.getInstance(TypedElementAdapter.class.getName());

	/**
	 */
	protected static final String ATTRNAME_TYPE = "type";

	/**
	 */
	protected class AttrAdapter extends NodeAdapter implements Attr
	{
		/**
		 */
		protected String name;
		protected String value;

		/**
		 * TODO:  build HierarchicalNode into the inheritance structure so that this constructor can be eliminated.
		 */
		public AttrAdapter(String name, String value)
		{
			super(null, 0);

			this.name = name;
			this.value = value;
		}

		/**
		 */
		public short getNodeType()
		{
			return ATTRIBUTE_NODE;
		}

		public String getBaseURI() {
			return null;
		}

		public short compareDocumentPosition(Node other) throws DOMException {
			return 0;
		}

		public String getTextContent() throws DOMException {
			return null;
		}

		public void setTextContent(String textContent) throws DOMException {

		}

		public boolean isSameNode(Node other) {
			return false;
		}

		public String lookupPrefix(String namespaceURI) {
			return null;
		}

		public boolean isDefaultNamespace(String namespaceURI) {
			return false;
		}

		public String lookupNamespaceURI(String prefix) {
			return null;
		}

		public boolean isEqualNode(Node arg) {
			return false;
		}

		public Object getFeature(String feature, String version) {
			return null;
		}

		public Object setUserData(String key, Object data, UserDataHandler handler) {
			return null;
		}

		public Object getUserData(String key) {
			return null;
		}

		/**
		 * What is the defined behavior for this supposed to be?
		 */
		public String getNodeName()
		{
			return this.getName();
		}

		/**
		 * What is the defined behavior for this supposed to be?
		 */
		public String getNodeValue()
		{
			return this.getValue();
		}

		/**
		 */
		public String getName()
		{
			return this.name;
		}

		/**
		 * We think this should be true.  Maybe.
		 */
		public boolean getSpecified()
		{
			return true;
		}

		/**
		 * Obtains the class name of the wrapped object.
		 */
		public String getValue()
		{
			//return wrapped.getClass().getName();
			return this.value;
		}

		/**
		 */
		public void setValue(String value) throws DOMException
		{
			throw new UnsupportedOperationException();
		}

		/**
		 */
		public Element getOwnerElement()
		{
			return TypedElementAdapter.this;
		}

		public TypeInfo getSchemaTypeInfo() {
			return null;
		}

		public boolean isId() {
			return false;
		}

		/**
		 * @see Node#supports(String, String)
		 */
		public boolean supports(String arg0, String arg1) {
			throw new UnsupportedOperationException("supports");
		}

	}

	/**
	 */
	protected class TypeNamedNodeMapAdapter extends NamedNodeMapUnimplemented
	{
		protected Node typeAttr = new AttrAdapter(ATTRNAME_TYPE, wrapped.getClass().getName());

		/** */
		public Node getNamedItem(String name)
		{
			log.debug("NamedNodeMapAdapter.getNamedItem(" + name + ") is the type attribute");

			return typeAttr;
		}

		/** */
		public Node item(int index)
		{
			log.debug("NamedNodeMapAdapter.item(" + index + ") is the type attribute");

			return typeAttr;
		}

		/** */
		public int getLength()
		{
			log.debug("NamedNodeMapAdapter.getLength() is 1");

			return 1;
		}

		/** */
		public Node getNamedItemNS(String namespaceURI, String localName)
		{
			log.debug("NamedNodeMapAdapter.getNamedItemNS()");

			return null;
		}
	}

	/**
	 * This is lazy-created.
	 */
	protected NamedNodeMap namedNodeMapAdapter = null;

	/**
	 */
	protected TypedElementAdapter(Object wrapme, String name, Node parent, int ordinal, DOMAdapter config)
	{
		super(wrapme, name, parent, ordinal, config);

		log.debug("Actually it was a TypedElementAdapter");
	}

	/**
	 * Override this in subclass to change available attrs
	 */
	protected NamedNodeMap getNamedNodeMapAdapter()
	{
		if (this.namedNodeMapAdapter == null)
			this.namedNodeMapAdapter = new TypeNamedNodeMapAdapter();

		return this.namedNodeMapAdapter;
	}

	/**
	 * Override this in subclass to change available attrs
	 */
	public boolean hasAttribute(String name)
	{
		log.debug("hasAttribute(" + name + ")");

		return (name.equals(ATTRNAME_TYPE));
	}

	/**
	 */
	public NamedNodeMap getAttributes()
	{
		log.debug("getAttributes()");

		return getNamedNodeMapAdapter();
	}

	/**
	 */
	public String getAttribute(String name)
	{
		log.debug("getAttribute(" + name + ")");

		if (this.hasAttribute(name))
			return ((Attr)getNamedNodeMapAdapter().getNamedItem(name)).getValue();
		else
			return "";
	}

	/**
	 */
	public Attr getAttributeNode(String name)
	{
		log.debug("getAttributeNode(" + name + ")");

		if (this.hasAttribute(name))
			return (Attr)getNamedNodeMapAdapter().getNamedItem(name);
		else
			return null;
	}

	/**
	 * Retrieves an attribute value by local name and namespace URI. HTML-only
	 * DOM implementations do not need to implement this method.
	 * @param namespaceURIThe namespace URI of the attribute to retrieve.
	 * @param localNameThe local name of the attribute to retrieve.
	 * @return The <code>Attr</code> value as a string, or the empty string
	 *   if that attribute does not have a specified or default value.
	 * @since DOM Level 2
	 */
	public String getAttributeNS(String namespaceURI,
								 String localName)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieves an <code>Attr</code> node by local name and namespace URI.
	 * HTML-only DOM implementations do not need to implement this method.
	 * @param namespaceURIThe namespace URI of the attribute to retrieve.
	 * @param localNameThe local name of the attribute to retrieve.
	 * @return The <code>Attr</code> node with the specified attribute local
	 *   name and namespace URI or <code>null</code> if there is no such
	 *   attribute.
	 * @since DOM Level 2
	 */
	public Attr getAttributeNodeNS(String namespaceURI,
								   String localName)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns <code>true</code> when an attribute with a given local name and
	 * namespace URI is specified on this element or has a default value,
	 * <code>false</code> otherwise. HTML-only DOM implementations do not
	 * need to implement this method.
	 * @param namespaceURIThe namespace URI of the attribute to look for.
	 * @param localNameThe local name of the attribute to look for.
	 * @return <code>true</code> if an attribute with the given local name
	 *   and namespace URI is specified or has a default value on this
	 *   element, <code>false</code> otherwise.
	 * @since DOM Level 2
	 */
	public boolean hasAttributeNS(String namespaceURI,
								  String localName)
	{
		throw new UnsupportedOperationException();
	}
}
