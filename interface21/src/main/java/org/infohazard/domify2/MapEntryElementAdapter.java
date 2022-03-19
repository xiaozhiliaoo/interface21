/*
 * $Id: MapEntryElementAdapter.java,v 1.1.1.1 2002/08/01 12:52:05 Rod Johnson Exp $
 * $Source: c:\\work\\cvs/framework/src/org/infohazard/domify2/MapEntryElementAdapter.java,v $
 */

package org.infohazard.domify2;

import org.apache.log4j.Category;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Map;


/**
 */
public class MapEntryElementAdapter extends TypedElementAdapter
{
	/** Logging category for log4j. */
	protected static Category log = Category.getInstance(MapEntryElementAdapter.class.getName());

	/**
	 */
	protected static final String ATTRNAME_KEY = "key";

	/**
	 */
	protected class MapEntryNamedNodeMapAdapter extends NamedNodeMapUnimplemented
	{
		Node typeAttr = new AttrAdapter(ATTRNAME_TYPE, wrapped.getClass().getName());
		Node keyAttr = new AttrAdapter(ATTRNAME_KEY, mapKey);

		/** */
		public Node getNamedItem(String name)
		{
			log.debug("MapEntryNamedNodeMapAdapter.getNamedItem(" + name + ")");

			if (name.equals(ATTRNAME_KEY))
				return keyAttr;
			else if (name.equals(ATTRNAME_TYPE))
				return typeAttr;
			else
				return null;
		}

		/** */
		public Node item(int index)
		{
			log.debug("MapEntryNamedNodeMapAdapter.item(" + index + ")");

			if (index == 0)
				return keyAttr;
			else if (index == 1)
				return typeAttr;
			else
				return null;
		}

		/** */
		public int getLength()
		{
			log.debug("MapEntryNamedNodeMapAdapter.getLength() is 2");

			return 2;
		}

		/** */
		public Node getNamedItemNS(String namespaceURI, String localName)
		{
			log.debug("MapEntryNamedNodeMapAdapter.getNamedItemNS()");

			return null;
		}
	}

	/**
	 */
	protected String mapKey;

	/**
	 * This is lazy-created.
	 */
	protected NamedNodeMap namedNodeMapAdapter = null;

	/**
	 */
	protected MapEntryElementAdapter(Map.Entry me, String name, Node parent, int ordinal, DOMAdapter config)
	{
		super(me.getValue(), name, parent, ordinal, config);

		// Convert everything to string, if possible
		mapKey = me.getKey().toString();

		log.debug("Actually it was a MapEntryElementAdapter");
	}

	/**
	 * Override this in subclass to change available attrs
	 */
	protected NamedNodeMap getNamedNodeMapAdapter()
	{
		if (this.namedNodeMapAdapter == null)
			this.namedNodeMapAdapter = new MapEntryNamedNodeMapAdapter();

		return this.namedNodeMapAdapter;
	}

	/**
	 * Override this in subclass to change available attrs
	 */
	public boolean hasAttribute(String name)
	{
		log.debug("hasAttribute(" + name + ")");

		return (name.equals(ATTRNAME_TYPE) || name.equals(ATTRNAME_KEY));
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
