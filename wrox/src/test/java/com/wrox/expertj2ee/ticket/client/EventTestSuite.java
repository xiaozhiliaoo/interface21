/*
package com.wrox.expertj2ee.ticket.client;


import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator; 
import java.util.Map; 

import javax.sql.DataSource;
import jdbc.*;
import jdbc.TestDataSource;

import junit.framework.TestCase;
import org.apache.xalan.serialize.SerializerToXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.interface21.core.Assertions;
import com.interface21.core.NodeCountVisitor;
import com.interface21.tree.Directory;
import com.interface21.tree.DirectoryElementFactory;
import com.interface21.tree.IndexableTreeNode;
import com.interface21.tree.IndexableTreeNodeElementFactory;
import com.interface21.tree.IndexableTreeNodeSupport;
import com.interface21.tree.LinkDefinition;
import com.interface21.tree.NoSuchTreeNodeException;
import com.interface21.tree.TreeIndex;
import com.interface21.tree.UndefinedLinkException;
import com.interface21.tree.UnresolvableLinkException;
import com.interface21.xml.ElementFactory;
import com.interface21.xml.XmlVisitor;
import com.wrox.j2eedd.ticket.common.referencedata.*;
import com.wrox.j2eedd.ticket.ejb.referencedata.jdbc.*;

*/
/**
 *
 * @author  rod
 * @version 
 *//*

public class EventTestSuite extends TestCase {

	//public final static String FILENAME = "C:\\work\\pas\\prototype\\war\\WEB-INF\\applicationContext.xml,C:\\work\\pas\\prototype\\war\\WEB-INF\\integra-servlet.xml";

	// Use default database location
	DataSource ds = new TestDataSource();

	*/
/** Creates new SeatingPlanTest *//*

	public EventTestSuite(String name) {
		super(name);
	}

	*/
/** Run for each test *//*

	protected void setUp() throws Exception {

	}

	protected void tearDown() {
	}

	public void testEmptyDirectory() throws Exception {
		Directory dir = new Directory();
		assertTrue("contains 0 nodes", dir.getNodeCount() == 0);
		try {
			dir.getNode(String.class, 1);
			fail("Shouldn't find bogus node");
		}
		catch (NoSuchTreeNodeException ex) {
			// OK
		}
	}

	public void testUndefinedLink() throws Exception {
		Directory dir = new Directory();
		assertTrue("contains 0 nodes", dir.getNodeCount() == 0);
		IndexableTreeNodeSupport n = new IndexableTreeNodeSupport(14, "Parsifal");
		n.register(dir, null);
		try {
			IndexableTreeNode n2 = dir.getNode(IndexableTreeNodeSupport.class, 14);
			assertTrue("node out == node in", n2 == n);
			Object link = n2.followLink("foobar");
			fail("Node with undefined link should fail");
		}
		catch (UndefinedLinkException ex) {
			// What if multiple links defined?
			assertTrue("foobar link is undefined", ex.getLinkName().equals("foobar"));
		}
	}

	public void testUnresolvedNonNullableLink() throws Exception {
		Directory dir = new Directory();
		assertTrue("contains 0 nodes", dir.getNodeCount() == 0);
		Performance p = new Performance(11, "Parsifal");

		try {
			p.register(dir, null);
			//Performance p1 = (Performance) dir.getNode(Performance.class, 11);
			//Venue v = p1.getVenue();
			// Should fail early, rather than late (when it might be being used by a view)
			fail("Adding node with unresolvable link should fail");
		}
		catch (UnresolvableLinkException ex) {
			assertTrue("Venue link is undefined: didn't expect " + ex.getLinkName(), ex.getLinkName().equals("venue"));
		}
	}
	
	private class NullableVenuePerformance extends Performance {
		public NullableVenuePerformance(int id, String name) {
			super(id, name);
			// override link to make it nullable
			addLinkDefinition(new LinkDefinition("venue", Venue.class, true));
		}
	}
	
	public void testUnresolvedNullableLink() throws Exception {
		Directory dir = new Directory();
		assertTrue("contains 0 nodes", dir.getNodeCount() == 0);		
		NullableVenuePerformance p = new NullableVenuePerformance(12, "Parsifal");
		p.register(dir, null);
		assertTrue("venue is null", p.getVenue() == null);
	}

	// Parent not set?

	public void testResolvedLink() throws Exception {
		Directory dir = new Directory();
		Venue v = new Venue(1, "Opera House");
		v.register(dir, null);

		Performance p = new Performance(11, "Parsifal");
		Map m = new HashMap();
		m.put("venue", new Integer(1));
		p.register(dir, m);
		Performance p1 = (Performance) dir.getNode(Performance.class, 11);
		assertTrue("Found performance", p1 != null);
		assertTrue("Performance points to correct venue", p1.getVenue() == v);
	}
	
	*/
/*
	public void testVenues() {
		TreeIndex index = new Directory();
		Venues venues = new Venues(1000, "venues");
		venues.register(index, null);
	}
	*//*

	

	public void testRoot() throws Exception {

		final TreeIndex dir = new Queries.DirectoryQuery().getReferenceData(ds);

		Collection kids = dir.getChildren();
		for (Iterator iterator = kids.iterator(); iterator.hasNext();) {
			checkTreeNode((IndexableTreeNode) iterator.next());
		}
		//assertTrue("genre collection size == dir size", c.size() == dir.getSize());
		System.out.println("Dir size=" + dir.getNodeCount());

		NodeCountVisitor nvc = new NodeCountVisitor() {
			protected void recordVisit(com.interface21.core.Visitable host) {
				super.recordVisit(host);
				if (dir != host) {
					// Must be a node
					assertTrue("Expected IndexableTreeNode, not " + host.getClass(), host instanceof IndexableTreeNode);
					IndexableTreeNode n = (IndexableTreeNode) host;
					assertTrue("node " + n + " is in directory", dir.containsNode(n));
				}
			}
		};
		dir.acceptVisitor(nvc, 0);
		assertTrue(
			"Visitor visited all nodes (plus directory): dir says " + dir.getNodeCount() + "; visitor said " + nvc.getNodeCount(),
			nvc.getNodeCount() == dir.getNodeCount() + 1);
		
		//dir.acceptVisitor(new PrettyPrintVisitor(), 0);
	}
	
	
	public void testXmlVisitor() {
		TreeIndex dir = new Queries.DirectoryQuery().getReferenceData(ds);
				
		XmlVisitor xv = new XmlVisitor("ticketplus");
		xv.addElementFactory(Directory.class, new DirectoryElementFactory("reference-data"));
		
		*/
/*
		xv.addElementFactory(Venue.class, new VenueElementFactory());
		xv.addElementFactory(Genre.class, new GenreElementFactory());
		xv.addElementFactory(Show.class, new ShowElementFactory());
		xv.addElementFactory(Performance.class, new PerformanceElementFactory());
		*//*

		
		xv.addElementFactory(IndexableTreeNodeSupport.class, new IndexableTreeNodeElementFactory());
//		xv.addElementFactory(Genre.class, new IndexableTreeNodeElementFactory("g));
		xv.addElementFactory(Performance.class, new PerformanceElementFactory());
		
		dir.acceptVisitor(xv, 0);
		Document doc = xv.getDocument();
		assertTrue("doc root is ticketplus", doc.getDocumentElement().getTagName().equals("ticketplus"));
		//System.out.println(toString(doc));
		toFile(toString(doc));
	}
 
	private void checkTreeNode(IndexableTreeNode tn) {
		//System.out.println(tn);
		Collection kids = tn.getChildren();
		for (Iterator iterator = kids.iterator(); iterator.hasNext();) {
			IndexableTreeNode kid = (IndexableTreeNode) iterator.next();
			assertTrue("Kids parent is parent", kid.getParent() == tn);
			checkTreeNode(kid);
		}

		specialChecks(tn);
	}

	private void specialChecks(IndexableTreeNode tn) {
		if (tn instanceof Performance) {
			System.out.println("Applying performance checks...");
			Performance p = (Performance) tn;
			assertTrue("performance has a non null venue", p.getVenue() != null);
			Show s = p.getShow();
			assertTrue("Show parent has performance child", s.getChildren().contains(p));
		}
	}
	
	
	
	public String toString(Node n) {
		//log4jCategory.debug("Serializing XML node to a String");
		/*/
/***TEMP
		// Use Apache or JAXP transform with no stylesheet
		SerializerToXML ser = new SerializerToXML();
		StringWriter sw = new StringWriter();
		ser.setWriter(sw);
		try {
			ser.serialize(n);
		}
		catch (IOException ex) {
			throw new RuntimeException("Unable to serialize XML node to String: " + ex);
		}

		//System.out.println("XML string is [" + sw.getBuffer() + "]");
		return sw.getBuffer().toString();
	}
	
	public void toFile(String s) {
		try {
			Writer w = new FileWriter("c:\\temp\\e.xml");
			w.write(s);
			w.flush();
			w.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	private class VenueElementFactory implements ElementFactory {
		*/
/**
		 * @see ElementFactory#createElement(Object)
		 *//*

		public Element createElement(Document doc, Object o) {
			Venue v = (Venue) o;
			Element e = doc.createElement("venue"); 
			e.setAttribute("id", "" + v.getId());
			e.setAttribute("name", "" + v.getName());
			return e;
		}
	}
	
	private class GenreElementFactory implements ElementFactory {
		*/
/**
		 * @see ElementFactory#createElement(Object)
		 *//*

		public Element createElement(Document doc, Object o) {
			Genre g = (Genre) o;
			Element e = doc.createElement("genre"); 
			e.setAttribute("id", "" + g.getId());
			e.setAttribute("name", "" + g.getName());
			return e;
		}
	}
	
	// could have abstract node factory!?
	
	private class ShowElementFactory implements ElementFactory {
		*/
/**
		 * @see ElementFactory#createElement(Object)
		 *//*

		public Element createElement(Document doc, Object o) {
			Show s = (Show) o;
			Element e = doc.createElement("show"); 
			e.setAttribute("id", "" + s.getId());
			e.setAttribute("name", "" + s.getName());
			return e;
		}
	}
	
	private class PerformanceElementFactory extends IndexableTreeNodeElementFactory {
		*/
/**
		 * @see ElementFactory#createElement(Object)
		 *//*

		public Element createElement(Document doc, Object o) {
			Performance p = (Performance) o;
			Element e = super.createElement(doc, o);
			e.setAttribute("venueId", "" + p.getVenue().getId());
			return e;
		}
	}

}*/
