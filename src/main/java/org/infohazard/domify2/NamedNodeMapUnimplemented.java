/*
 * $Id: NamedNodeMapUnimplemented.java,v 1.1.1.1 2002/08/01 12:52:05 Rod Johnson Exp $
 * $Source: c:\\work\\cvs/framework/src/org/infohazard/domify2/NamedNodeMapUnimplemented.java,v $
 */

package org.infohazard.domify2;

import org.w3c.dom.*;

/**
 */
class NamedNodeMapUnimplemented implements NamedNodeMap
{
	/** */
	public Node getNamedItem(String name)
	{
		throw new UnsupportedOperationException();
	}

	/** */
	public Node setNamedItem(Node arg) throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/** */
	public Node removeNamedItem(String name) throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/** */
	public Node item(int index)
	{
		throw new UnsupportedOperationException();
	}

	/** */
	public int getLength()
	{
		throw new UnsupportedOperationException();
	}

	/** */
	public Node getNamedItemNS(String namespaceURI, String localName)
	{
		throw new UnsupportedOperationException();
	}

	/** */
	public Node setNamedItemNS(Node arg) throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	/** */
	public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException
	{
		throw new UnsupportedOperationException();
	}
}
