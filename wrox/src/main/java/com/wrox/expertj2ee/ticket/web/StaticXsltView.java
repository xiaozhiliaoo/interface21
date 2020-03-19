
package com.wrox.expertj2ee.ticket.web;

import java.io.File;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.interface21.web.servlet.view.xslt.XsltView;

public class StaticXsltView extends XsltView {
	
	Document doc;
	
	public StaticXsltView() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		this.doc = db.parse(new File("C:\\work\\book\\ticket\\war\\xsl\\in.xml"));
		logger.warning("Cached XML document");
	}

	/**
	 * @see XsltView#createDomNodeToTransform(Map, String)
	 */
	protected Node createDomNodeToTransform(Map model, String root) {
		logger.warning("createDomNodeToTransform");
		return this.doc;
	}

}
