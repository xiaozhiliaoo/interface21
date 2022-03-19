/*
 * $Id: ElementAdapter.java,v 1.1.1.1 2002/08/01 12:52:05 Rod Johnson Exp $
 * $Source: c:\\work\\cvs/framework/src/org/infohazard/domify2/ElementAdapter.java,v $
 */

package org.infohazard.domify2;

import org.apache.log4j.Category;
import org.w3c.dom.*;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 */
public class ElementAdapter extends NodeAdapter implements Element
{
	/** Logging category for log4j. */
	protected static Category log = Category.getInstance(ElementAdapter.class.getName());

	/**
	 */
	String nodeName;

	/**
	 * Used by the NodeListAdapter
	 */
	protected static Object[] emptyArgList = new Object[0];

	/**
	 */
	protected class MethodNodeListAdapter implements NodeList
	{
		/**
		 *  Local list of nethods that meet our property definition below.
		 */
		protected List propertyMethods;
		
		/** Names of methods */
		protected List decapitalizedNames;

		/**
		 * creates a list of methods for the wrapped (in our parent class)
		 * object that meet the following criteria:
		 *
		 * public, no args req'd, non-void return,
		 * not defined by Object interface
		 * and starts with "get" or "is"
		 */
		public MethodNodeListAdapter()
		{
			//BeanInfo bi =
			Method[] methods = wrapped.getClass().getMethods();

			// A good outside bound
			this.propertyMethods = new ArrayList(methods.length);
			this.decapitalizedNames = new ArrayList(methods.length);

			for (int i=0; i<methods.length; i++)
			{
				Method m = methods[i];
				if (Modifier.isPublic(m.getModifiers()) &&
					m.getParameterTypes().length == 0 &&
					m.getReturnType() != null &&
					!m.getDeclaringClass().equals(Object.class) &&
					(m.getName().startsWith("get") || m.getName().startsWith("is")) &&
					!config.shouldIgnoreMethod(m)
					)
				{
					// add it!
					this.propertyMethods.add(m);
					
					// Cache decapitalized name: e.g. age for a getAge() method
					String decapitalizedName;
					if (m.getName().charAt(0) == 'i')
						// "is".length()
						decapitalizedName = m.getName().substring(2);
					else
					// "get".length()
						decapitalizedName = m.getName().substring(3);
					decapitalizedName = java.beans.Introspector.decapitalize(decapitalizedName);
					decapitalizedNames.add(decapitalizedName);
				}
			}
		}

		/**
		 * returns the length of the method exposed as nodes.
		 */
		public int getLength()
		{
			if (log.isDebugEnabled())
				log.debug("MethodNodeListAdapter.getLength() is " + this.propertyMethods.size());

			return this.propertyMethods.size();
		}

		/**
		 * retrieves a single node by index from our adapted object
		 */
		public Node item(int index)
		{
			if (log.isDebugEnabled())
				log.debug("MethodNodeListAdapter.item(" + index + ")");

			Method m = (Method)this.propertyMethods.get(index);

			try
			{
				Object returnValue = m.invoke(wrapped, emptyArgList);

				if (returnValue == wrapped)
					log.debug("MethodNodeListAdapter.item: " +
						"wrapped object returned itself, possible recusive call");
				return new ElementAdapter(returnValue, (String) decapitalizedNames.get(index), ElementAdapter.this, index, config);
			}
			catch (RuntimeException ex)
			{
				throw ex;
			}
			catch (InvocationTargetException ex)
			{
				// TODO:  Is this the best error message we can report?
				ex.printStackTrace();

				String message = "InvocationTargetException when invoking " + m + ":  " + ex.getTargetException();

				throw new RuntimeException(message);
			}
			catch (Exception ex)
			{
				// This should never happen because we chose the method for its
				// public access and no args.
				ex.printStackTrace();

				String message = "Exception when invoking " + m + ":  " + ex;

				throw new RuntimeException(message);
			}
		}
	}

	/**
	 */
	protected class ToStringNodeListAdapter implements NodeList
	{
		/** Cache the node */
		protected Node value;

		/** */
		public ToStringNodeListAdapter()
		{
		}

		/** */
		public int getLength()
		{
			if (log.isDebugEnabled())
				log.debug("ToStringNodeListAdapter.getLength() is 1");

			return 1;
		}

		/** */
		public Node item(int index)
		{
			if (log.isDebugEnabled())
				log.debug("ToStringNodeListAdapter.item(" + index + ")");

			if (value == null)
				value = new TextAdapter(wrapped.toString(), ElementAdapter.this, index);

			return value;
		}
	}

	/**
	 */
	protected class CollectionNodeListAdapter implements NodeList
	{
		/** Cache the node */
		protected List nodes;

		/** */
		public CollectionNodeListAdapter(Collection col)
		{
			//String newNodeName = nodeName + "Item";
			String newNodeName = "item";

			nodes = new ArrayList(col.size());

			int index = 0;
			Iterator it = col.iterator();
			while (it.hasNext())
			{
				ElementAdapter child = new TypedElementAdapter(it.next(), newNodeName, ElementAdapter.this, index, config);

				nodes.add(child);

				index++;
			}
		}

		/** The array parameter MUST BE AN ARRAY!  Can contain objects or basic types. */
		public CollectionNodeListAdapter(Object array)
		{
			//String newNodeName = nodeName + "Item";
			String newNodeName = "item";

			int length = Array.getLength(array);

			nodes = new ArrayList(length);

			for (int index=0; index<length; index++)
			{
				Object obj = Array.get(array, index);

				ElementAdapter child = new TypedElementAdapter(obj, newNodeName, ElementAdapter.this, index, config);

				nodes.add(child);
			}
		}

		/** */
		public int getLength()
		{
			if (log.isDebugEnabled())
				log.debug("CollectionNodeListAdapter.getLength() is " + nodes.size());

			return nodes.size();
		}

		/** */
		public Node item(int index)
		{
			if (log.isDebugEnabled())
				log.debug("CollectionNodeListAdapter.item(" + index + ")");

			return (Node)nodes.get(index);
		}
	}

	/**
	 */
	protected class MapNodeListAdapter implements NodeList
	{
		/** Cache the node */
		protected List nodes;

		/** */
		public MapNodeListAdapter(Map m)
		{
			//String newNodeName = nodeName + "Item";
			String newNodeName = "item";

			nodes = new ArrayList(m.size());

			int index = 0;
			Iterator it = m.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry mapEntry = (Map.Entry)it.next();

				MapEntryElementAdapter child = new MapEntryElementAdapter(mapEntry, newNodeName, ElementAdapter.this, index, config);

				nodes.add(child);

				index++;
			}
		}

		/** */
		public int getLength()
		{
			if (log.isDebugEnabled())
				log.debug("MapNodeListAdapter.getLength() is " + nodes.size());

			return nodes.size();
		}

		/** */
		public Node item(int index)
		{
			if (log.isDebugEnabled())
				log.debug("MapNodeListAdapter.item(" + index + ")");

			return (Node)nodes.get(index);
		}
	}

	/**
	 */
	protected static class EmptyNodeListAdapter implements NodeList
	{
		/** We only need one of these */
		public static final NodeList EMPTY_LIST = new EmptyNodeListAdapter();

		/** */
		public int getLength() { return 0; }

		/** */
		public Node item(int index) { throw new IllegalStateException(); }
	}

	/**
	 * The JavaBean object wrapped by this Element Node.
	 */
	protected Object wrapped;

	/**
	 * Needed for any children requests.
	 */
	protected NodeList nodeListAdapter = null;

	/**
	 */
	protected DOMAdapter config;

	/**
	 */
	protected ElementAdapter(Object wrapme, String name, Node parent, int ordinal, DOMAdapter config)
	{
		super(parent, ordinal);

		this.nodeName = name;
		this.config = config;
		this.wrapped = wrapme;

		if (wrapme != null && log.isDebugEnabled())
			log.debug("Created ElementAdapter for object of type " + wrapme.getClass().getName() + ", named:" + name);
	}

	/**
	 */
	protected NodeList getNodeListAdapter()
	{
		if (this.nodeListAdapter == null)
		{
			if (this.wrapped == null)
			{
				if (log.isDebugEnabled())
					log.debug("NodeList: returning new Empty_List");
				this.nodeListAdapter = EmptyNodeListAdapter.EMPTY_LIST;
			}
			else if (this.wrapped.getClass().isArray())
			{
				if (log.isDebugEnabled())
					log.debug("NodeList: returning new CollectionNodeListAdapter(base Array)");
				this.nodeListAdapter = new CollectionNodeListAdapter(this.wrapped);
			}
			else if (this.wrapped instanceof Collection)
			{
				if (log.isDebugEnabled())
					log.debug("NodeList: returning new CollectionNodeListAdapter (base Colleciton)");
				this.nodeListAdapter = new CollectionNodeListAdapter((Collection)this.wrapped);
			}
			else if (this.wrapped instanceof Map)
			{
				if (log.isDebugEnabled())
					log.debug("NodeList: returning new MapNodeListAdapter");
				this.nodeListAdapter = new MapNodeListAdapter((Map)this.wrapped);
			}
			else if (this.config.shouldConvertToString(this.wrapped))
			{
				if (log.isDebugEnabled())
					log.debug("NodeList: returning new ToStringNodeListAdapter");
				this.nodeListAdapter = new ToStringNodeListAdapter();
			}
			else
			{
				if (log.isDebugEnabled())
					log.debug("NodeList: returning new MethodNodeListAdapter");
				this.nodeListAdapter = new MethodNodeListAdapter();
			}
		}

		return this.nodeListAdapter;
	}

	/**
	 */
	public short getNodeType()
	{
		if (log.isDebugEnabled())
			log.debug("getNodeType(): wrapped=[" + wrapped + "]");

		return ELEMENT_NODE;
	}

	/**
	 */
	public String getNodeName()
	{
		if (log.isDebugEnabled())
			log.debug("getNodeName()");

		return this.nodeName;
	}

	/**
	 * Element nodes do not have node values.
	 */
	public String getNodeValue() throws DOMException
	{
		if (log.isDebugEnabled())
			log.debug("getNodeValue()");

		return null;
	}

	/**
	 * Introspect our object and see what child nodes are appropriate.
	 */
	public NodeList getChildNodes()
	{
		if (log.isDebugEnabled())
			log.debug("getChildNodes()");

		return this.getNodeListAdapter();
	}

	/**
	 */
	public NamedNodeMap getAttributes()
	{
		if (log.isDebugEnabled())
			log.debug("getAttributes()");

		return new NamedNodeMapUnimplemented() {
			public int getLength() { return 0; }
		};

	}

	/**
	 * Since we don't have namespaces, this is the same as getNodeName().
	 */
	public String getLocalName()
	{
		if (log.isDebugEnabled())
			log.debug("getLocalName()");

		return this.getNodeName();
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
	 * @return null if there are no children.
	 */
	public Node getFirstChild()
	{
		if (log.isDebugEnabled())
			log.debug("getFirstChild()");

		NodeList list = this.getNodeListAdapter();

		if (list.getLength() > 0)
			return this.getNodeListAdapter().item(0);
		else
			return null;
	}

	/**
	 * @return null if there are no children.
	 */
	public Node getLastChild()
	{
		if (log.isDebugEnabled())
			log.debug("getLastChild()");

		NodeList list = this.getNodeListAdapter();

		if (list.getLength() > 0)
			return list.item(list.getLength()-1);
		else
			return null;
	}

	/**
	 */
	public boolean hasChildNodes()
	{
		if (log.isDebugEnabled())
			log.debug("hasChildNodes()");

		NodeList list = this.getNodeListAdapter();

		if (list.getLength() > 0)
 		    return true;
 		else
 		    return false;
	}
	/**
	 * The name of the element. For example, in:
	 * <pre> &lt;elementExample
	 * id="demo"&gt; ... &lt;/elementExample&gt; , </pre>
	 *  <code>tagName</code> has
	 * the value <code>"elementExample"</code>. Note that this is
	 * case-preserving in XML, as are all of the operations of the DOM. The
	 * HTML DOM returns the <code>tagName</code> of an HTML element in the
	 * canonical uppercase form, regardless of the case in the source HTML
	 * document.
	 */
	public String getTagName()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieves an attribute value by name.
	 * @param nameThe name of the attribute to retrieve.
	 * @return The <code>Attr</code> value as a string, or the empty string
	 *   if that attribute does not have a specified or default value.
	 */
	public String getAttribute(String name)
	{
		throw new UnsupportedOperationException();
	}


	/**
	 * Adds a new attribute. If an attribute with that name is already present
	 * in the element, its value is changed to be that of the value
	 * parameter. This value is a simple string; it is not parsed as it is
	 * being set. So any markup (such as syntax to be recognized as an
	 * entity reference) is treated as literal text, and needs to be
	 * appropriately escaped by the implementation when it is written out.
	 * In order to assign an attribute value that contains entity
	 * references, the user must create an <code>Attr</code> node plus any
	 * <code>Text</code> and <code>EntityReference</code> nodes, build the
	 * appropriate subtree, and use <code>setAttributeNode</code> to assign
	 * it as the value of an attribute.
	 * <br>To set an attribute with a qualified name and namespace URI, use
	 * the <code>setAttributeNS</code> method.
	 * @param nameThe name of the attribute to create or alter.
	 * @param valueValue to set in string form.
	 * @exception DOMException
	 *   INVALID_CHARACTER_ERR: Raised if the specified name contains an
	 *   illegal character.
	 *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 */
	public void setAttribute(String name,
							 String value)
							 throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Removes an attribute by name. If the removed attribute is known to have
	 * a default value, an attribute immediately appears containing the
	 * default value as well as the corresponding namespace URI, local name,
	 * and prefix when applicable.
	 * <br>To remove an attribute by local name and namespace URI, use the
	 * <code>removeAttributeNS</code> method.
	 * @param nameThe name of the attribute to remove.
	 * @exception DOMException
	 *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 */
	public void removeAttribute(String name)
								throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieves an attribute node by name.
	 * <br>To retrieve an attribute node by qualified name and namespace URI,
	 * use the <code>getAttributeNodeNS</code> method.
	 * @param nameThe name (<code>nodeName</code>) of the attribute to
	 *   retrieve.
	 * @return The <code>Attr</code> node with the specified name (
	 *   <code>nodeName</code>) or <code>null</code> if there is no such
	 *   attribute.
	 */
	public Attr getAttributeNode(String name)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Adds a new attribute node. If an attribute with that name (
	 * <code>nodeName</code>) is already present in the element, it is
	 * replaced by the new one.
	 * <br>To add a new attribute node with a qualified name and namespace
	 * URI, use the <code>setAttributeNodeNS</code> method.
	 * @param newAttrThe <code>Attr</code> node to add to the attribute list.
	 * @return If the <code>newAttr</code> attribute replaces an existing
	 *   attribute, the replaced <code>Attr</code> node is returned,
	 *   otherwise <code>null</code> is returned.
	 * @exception DOMException
	 *   WRONG_DOCUMENT_ERR: Raised if <code>newAttr</code> was created from a
	 *   different document than the one that created the element.
	 *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 *   <br>INUSE_ATTRIBUTE_ERR: Raised if <code>newAttr</code> is already an
	 *   attribute of another <code>Element</code> object. The DOM user must
	 *   explicitly clone <code>Attr</code> nodes to re-use them in other
	 *   elements.
	 */
	public Attr setAttributeNode(Attr newAttr)
								 throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Removes the specified attribute node. If the removed <code>Attr</code>
	 * has a default value it is immediately replaced. The replacing
	 * attribute has the same namespace URI and local name, as well as the
	 * original prefix, when applicable.
	 * @param oldAttrThe <code>Attr</code> node to remove from the attribute
	 *   list.
	 * @return The <code>Attr</code> node that was removed.
	 * @exception DOMException
	 *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 *   <br>NOT_FOUND_ERR: Raised if <code>oldAttr</code> is not an attribute
	 *   of the element.
	 */
	public Attr removeAttributeNode(Attr oldAttr)
									throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns a <code>NodeList</code> of all descendant <code>Elements</code>
	 * with a given tag name, in the order in which they are encountered in
	 * a preorder traversal of this <code>Element</code> tree.
	 * @param nameThe name of the tag to match on. The special value "*"
	 *   matches all tags.
	 * @return A list of matching <code>Element</code> nodes.
	 */
	public NodeList getElementsByTagName(String name)
	{
		throw new UnsupportedOperationException();
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
	 * Adds a new attribute. If an attribute with the same local name and
	 * namespace URI is already present on the element, its prefix is
	 * changed to be the prefix part of the <code>qualifiedName</code>, and
	 * its value is changed to be the <code>value</code> parameter. This
	 * value is a simple string; it is not parsed as it is being set. So any
	 * markup (such as syntax to be recognized as an entity reference) is
	 * treated as literal text, and needs to be appropriately escaped by the
	 * implementation when it is written out. In order to assign an
	 * attribute value that contains entity references, the user must create
	 * an <code>Attr</code> node plus any <code>Text</code> and
	 * <code>EntityReference</code> nodes, build the appropriate subtree,
	 * and use <code>setAttributeNodeNS</code> or
	 * <code>setAttributeNode</code> to assign it as the value of an
	 * attribute.
	 * <br>HTML-only DOM implementations do not need to implement this method.
	 * @param namespaceURIThe namespace URI of the attribute to create or
	 *   alter.
	 * @param qualifiedNameThe qualified name of the attribute to create or
	 *   alter.
	 * @param valueThe value to set in string form.
	 * @exception DOMException
	 *   INVALID_CHARACTER_ERR: Raised if the specified qualified name
	 *   contains an illegal character.
	 *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 *   <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is
	 *   malformed, if the <code>qualifiedName</code> has a prefix and the
	 *   <code>namespaceURI</code> is <code>null</code>, if the
	 *   <code>qualifiedName</code> has a prefix that is "xml" and the
	 *   <code>namespaceURI</code> is different from "
	 *   http://www.w3.org/XML/1998/namespace", or if the
	 *   <code>qualifiedName</code> is "xmlns" and the
	 *   <code>namespaceURI</code> is different from "
	 *   http://www.w3.org/2000/xmlns/".
	 * @since DOM Level 2
	 */
	public void setAttributeNS(String namespaceURI,
							   String qualifiedName,
							   String value)
							   throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Removes an attribute by local name and namespace URI. If the removed
	 * attribute has a default value it is immediately replaced. The
	 * replacing attribute has the same namespace URI and local name, as
	 * well as the original prefix.
	 * <br>HTML-only DOM implementations do not need to implement this method.
	 * @param namespaceURIThe namespace URI of the attribute to remove.
	 * @param localNameThe local name of the attribute to remove.
	 * @exception DOMException
	 *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 * @since DOM Level 2
	 */
	public void removeAttributeNS(String namespaceURI,
								  String localName)
								  throws DOMException
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
	 * Adds a new attribute. If an attribute with that local name and that
	 * namespace URI is already present in the element, it is replaced by
	 * the new one.
	 * <br>HTML-only DOM implementations do not need to implement this method.
	 * @param newAttrThe <code>Attr</code> node to add to the attribute list.
	 * @return If the <code>newAttr</code> attribute replaces an existing
	 *   attribute with the same local name and namespace URI, the replaced
	 *   <code>Attr</code> node is returned, otherwise <code>null</code> is
	 *   returned.
	 * @exception DOMException
	 *   WRONG_DOCUMENT_ERR: Raised if <code>newAttr</code> was created from a
	 *   different document than the one that created the element.
	 *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 *   <br>INUSE_ATTRIBUTE_ERR: Raised if <code>newAttr</code> is already an
	 *   attribute of another <code>Element</code> object. The DOM user must
	 *   explicitly clone <code>Attr</code> nodes to re-use them in other
	 *   elements.
	 * @since DOM Level 2
	 */
	public Attr setAttributeNodeNS(Attr newAttr)
								   throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns a <code>NodeList</code> of all the descendant
	 * <code>Elements</code> with a given local name and namespace URI in
	 * the order in which they are encountered in a preorder traversal of
	 * this <code>Element</code> tree.
	 * <br>HTML-only DOM implementations do not need to implement this method.
	 * @param namespaceURIThe namespace URI of the elements to match on. The
	 *   special value "*" matches all namespaces.
	 * @param localNameThe local name of the elements to match on. The
	 *   special value "*" matches all local names.
	 * @return A new <code>NodeList</code> object containing all the matched
	 *   <code>Elements</code>.
	 * @since DOM Level 2
	 */
	public NodeList getElementsByTagNameNS(String namespaceURI,
										   String localName)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns <code>true</code> when an attribute with a given name is
	 * specified on this element or has a default value, <code>false</code>
	 * otherwise.
	 * @param nameThe name of the attribute to look for.
	 * @return <code>true</code> if an attribute with the given name is
	 *   specified on this element or has a default value, <code>false</code>
	 *	otherwise.
	 * @since DOM Level 2
	 */
	public boolean hasAttribute(String name)
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

	public TypeInfo getSchemaTypeInfo() {
		return null;
	}

	public void setIdAttribute(String name, boolean isId) throws DOMException {

	}

	public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {

	}

	public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {

	}

	/**
	 * @see Node#supports(String, String)
	 */
	public boolean supports(String arg0, String arg1) {
		throw new UnsupportedOperationException("supports");
	}

}
