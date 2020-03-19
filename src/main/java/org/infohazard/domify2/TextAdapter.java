/*
 * $Id: TextAdapter.java,v 1.1.1.1 2002/08/01 12:52:05 Rod Johnson Exp $
 * $Source: c:\\work\\cvs/framework/src/org/infohazard/domify2/TextAdapter.java,v $
 */

package org.infohazard.domify2;

import org.apache.log4j.Category;
import org.w3c.dom.*;

/**
 */
public class TextAdapter extends NodeAdapter implements Text
{
	/** Logging category for log4j. */
	protected static Category log = Category.getInstance(TextAdapter.class.getName());

	/**
	 */
	protected static final char CR = 0x0d;
	protected static final char LF = 0x0a;

	/**
	 */
	protected String wrapped;

	/**
	 */
	protected TextAdapter(String wrapme, Node parent, int ordinal)
	{
		super(parent, ordinal);

		// Convert any instance of CRLF or CR by itself to just LF, as required by
		// the XML specification:  http://www.w3.org/TR/REC-xml#sec-line-ends

		StringBuffer buf = new StringBuffer(wrapme.length());

		boolean ignoreLF = false;

		for (int i=0; i<wrapme.length(); i++)
		{
			char c = wrapme.charAt(i);

			if (c == CR)
			{
				buf.append(LF);
				ignoreLF = true;
			}
			else if (c == LF)
			{
				if (!ignoreLF)
					buf.append(LF);

				ignoreLF = false;
			}
			else
			{
				buf.append(c);

				ignoreLF = false;
			}
		}

		this.wrapped = buf.toString();
	}

	/**
	 */
	public short getNodeType()
	{
		if (log.isDebugEnabled())
			log.debug("getNodeType()");

		return TEXT_NODE;
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
	 */
	public String getNodeName()
	{
		log.debug("getNodeName()");

		return "#text";
	}

	/**
	 * @return the text of this node; this is the whole point, really.
	 */
	public String getNodeValue() throws DOMException
	{
		if (log.isDebugEnabled())
			log.debug("getNodeValue()");

		return this.wrapped;
	}

	/**
	 * @return the text of this node; this is the whole point, really.
	 */
	public String getData() throws DOMException
	{
		if (log.isDebugEnabled())
			log.debug("getData()");

		return this.wrapped;
	}

	/**
	 * Breaks this node into two nodes at the specified <code>offset</code>,
	 * keeping both in the tree as siblings. After being split, this node
	 * will contain all the content up to the <code>offset</code> point. A
	 * new node of the same type, which contains all the content at and
	 * after the <code>offset</code> point, is returned. If the original
	 * node had a parent node, the new node is inserted as the next sibling
	 * of the original node. When the <code>offset</code> is equal to the
	 * length of this node, the new node has no data.
	 * @param offsetThe 16-bit unit offset at which to split, starting from
	 *   <code>0</code>.
	 * @return The new node, of the same type as this node.
	 * @exception DOMException
	 *   INDEX_SIZE_ERR: Raised if the specified offset is negative or greater
	 *   than the number of 16-bit units in <code>data</code>.
	 *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 */
	public Text splitText(int offset)
						  throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	public boolean isElementContentWhitespace() {
		return false;
	}

	public String getWholeText() {
		return null;
	}

	public Text replaceWholeText(String content) throws DOMException {
		return null;
	}

	/**
	 * The character data of the node that implements this interface. The DOM
	 * implementation may not put arbitrary limits on the amount of data
	 * that may be stored in a <code>CharacterData</code> node. However,
	 * implementation limits may mean that the entirety of a node's data may
	 * not fit into a single <code>DOMString</code>. In such cases, the user
	 * may call <code>substringData</code> to retrieve the data in
	 * appropriately sized pieces.
	 * @exception DOMException
	 *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
	 * @exception DOMException
	 *   DOMSTRING_SIZE_ERR: Raised when it would return more characters than
	 *   fit in a <code>DOMString</code> variable on the implementation
	 *   platform.
	 */
	public void setData(String data) throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * The number of 16-bit units that are available through <code>data</code>
	 * and the <code>substringData</code> method below. This may have the
	 * value zero, i.e., <code>CharacterData</code> nodes may be empty.
	 */
	public int getLength()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Extracts a range of data from the node.
	 * @param offsetStart offset of substring to extract.
	 * @param countThe number of 16-bit units to extract.
	 * @return The specified substring. If the sum of <code>offset</code> and
	 *   <code>count</code> exceeds the <code>length</code>, then all 16-bit
	 *   units to the end of the data are returned.
	 * @exception DOMException
	 *   INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is
	 *   negative or greater than the number of 16-bit units in
	 *   <code>data</code>, or if the specified <code>count</code> is
	 *   negative.
	 *   <br>DOMSTRING_SIZE_ERR: Raised if the specified range of text does
	 *   not fit into a <code>DOMString</code>.
	 */
	public String substringData(int offset,
								int count)
								throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Append the string to the end of the character data of the node. Upon
	 * success, <code>data</code> provides access to the concatenation of
	 * <code>data</code> and the <code>DOMString</code> specified.
	 * @param argThe <code>DOMString</code> to append.
	 * @exception DOMException
	 *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 */
	public void appendData(String arg)
						   throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Insert a string at the specified 16-bit unit offset.
	 * @param offsetThe character offset at which to insert.
	 * @param argThe <code>DOMString</code> to insert.
	 * @exception DOMException
	 *   INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is
	 *   negative or greater than the number of 16-bit units in
	 *   <code>data</code>.
	 *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 */
	public void insertData(int offset,
						   String arg)
						   throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Remove a range of 16-bit units from the node. Upon success,
	 * <code>data</code> and <code>length</code> reflect the change.
	 * @param offsetThe offset from which to start removing.
	 * @param countThe number of 16-bit units to delete. If the sum of
	 *   <code>offset</code> and <code>count</code> exceeds
	 *   <code>length</code> then all 16-bit units from <code>offset</code>
	 *   to the end of the data are deleted.
	 * @exception DOMException
	 *   INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is
	 *   negative or greater than the number of 16-bit units in
	 *   <code>data</code>, or if the specified <code>count</code> is
	 *   negative.
	 *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 */
	public void deleteData(int offset,
						   int count)
						   throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Replace the characters starting at the specified 16-bit unit offset
	 * with the specified string.
	 * @param offsetThe offset from which to start replacing.
	 * @param countThe number of 16-bit units to replace. If the sum of
	 *   <code>offset</code> and <code>count</code> exceeds
	 *   <code>length</code>, then all 16-bit units to the end of the data
	 *   are replaced; (i.e., the effect is the same as a <code>remove</code>
	 *	method call with the same range, followed by an <code>append</code>
	 *	method invocation).
	 * @param argThe <code>DOMString</code> with which the range must be
	 *   replaced.
	 * @exception DOMException
	 *   INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is
	 *   negative or greater than the number of 16-bit units in
	 *   <code>data</code>, or if the specified <code>count</code> is
	 *   negative.
	 *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 */
	public void replaceData(int offset,
							int count,
							String arg)
							throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see Node#supports(String, String)
	 */
	public boolean supports(String arg0, String arg1) {
		throw new UnsupportedOperationException("supports");
	}

}
