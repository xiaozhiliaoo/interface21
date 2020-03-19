/*
package populate;
import java.io.File;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.log4j.BasicConfigurator;
import org.infohazard.domify2.DOMAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import servletapi.TestHttpRequest;

import com.interface21.util.StopWatch;
import com.interface21.web.servlet.view.RequestInfo;
import com.interface21.web.servlet.view.xslt.XsltView;

import com.wrox.expertj2ee.ticket.boxoffice.ReservationRequest;
import com.wrox.expertj2ee.ticket.boxoffice.Seat;
import com.wrox.expertj2ee.ticket.boxoffice.support.ReservationImpl;
import com.wrox.expertj2ee.ticket.referencedata.PriceBand;
import com.wrox.expertj2ee.ticket.referencedata.support.PerformanceImpl;
import com.wrox.expertj2ee.ticket.referencedata.support.PriceBandImpl;
import com.wrox.expertj2ee.ticket.referencedata.support.ShowImpl;

public class domify {

	*/
/**
	 * Constructor for domify.
	 *//*

	public domify() {
		super();
	}
	
	static Map model;

	private static Map getModelMap() throws Exception {
		HashMap m = new HashMap();
		ReservationRequest rr = new ReservationRequest(1, 1, 2, true, 3.5, 5);
		Seat[] seats = new Seat[] { new Seat(1, "A1", 1, 0, 0), new Seat(2, "A2", 1, 0, 0)};
		m.put("reservation", new ReservationImpl(rr, seats, true, 10, "AA"));
		PerformanceImpl p = new PerformanceImpl(1, "1", 1, new Date());
		ShowImpl s = new ShowImpl(1, "Romeo and Juliet", 1);
		p.setShow(s);
		s.addPerformance(p);
		m.put("performance", p);
		PriceBand pb = new PriceBandImpl(1, 1, "AA", "Premium Reserve", 102.5);
		PriceBand pb2 = new PriceBandImpl(2, 2, "A", "B Reserve", 85.5);
		p.addPriceBand(pb);
		p.addPriceBand(pb2);
		m.put("priceband", pb);
		TestHttpRequest request = new TestHttpRequest(null, "GET", "test.html");
		request.addPreferredLocale(Locale.getDefault());
		RequestInfo ri = new RequestInfo(request);
		m.put(XsltView.REQUEST_INFO_KEY, ri);
		return m;
	}

	
	static	DOMAdapter domAdapter = new DOMAdapter();
	static {
		domAdapter.ignoreProperty(ShowImpl.class, "performances");
		domAdapter.ignoreProperty(PerformanceImpl.class, "priceBands");
	}
	
	static Templates templates;
	
	private static Templates createTemplates() throws Exception {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Source xmls = new StreamSource(new File("C:\\work\\book\\ticket\\war\\xsl\\showReservation-blanks.xsl"));
		return transformerFactory.newTemplates(xmls);
	}
	
	
	private static Document doc;
	
	private static Document getXmlDoc() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new File("C:\\work\\book\\ticket\\war\\xsl\\in.xml"));
		return doc;
	}
	
	
	private static Source getXml(boolean domify, Map model) {
		if (domify) {
			Node node = domAdapter.adapt(model, "reservationInfo");
		//	System.out.println(node);
			return new DOMSource(node);
		}
		else {
			return new DOMSource(doc);
		}
	}


	*/
/** NEED TEMPLATES ALSO *//*

	private static void transform(Source xml) throws Exception {
		Transformer trans = templates.newTransformer(); // just a copy
		//Source sss = new StreamSource(new File("C:\\work\\book\\ticket\\war\\xsl\\showReservation.xsl"));
		//Transformer trans = transformerFactory.newTransformer(sss);

		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		StreamResult sr = new StreamResult(new StringWriter()) ;//new StreamResult(System.out);
		trans.transform(xml, sr);
	}

	public static void main(String[] args) {
		//Logger.getLogger(domify.class.getName());

		// Configure Log4j
		BasicConfigurator.configure();
		
		int tests = 40;
		
		try {
			templates = createTemplates();
			doc = getXmlDoc();
			model = getModelMap();
			
			StopWatch sw = new StopWatch();
			sw.start(tests + "x with domify");
			for (int i = 0; i < tests; i++) {
				transform(getXml(true, model));
			}
			sw.stop();
			sw.start("with cached document");
			for (int i = 0; i < tests; i++) {
				transform(getXml(false,model));
			}
			sw.stop();
			sw.prettyPrint();
			System.out.println(sw);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}*/
