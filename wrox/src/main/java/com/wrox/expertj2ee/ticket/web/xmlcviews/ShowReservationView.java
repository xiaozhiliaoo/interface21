package com.wrox.expertj2ee.ticket.web.xmlcviews;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.enhydra.xml.xmlc.XMLObject;
import org.enhydra.xml.xmlc.servlet.XMLCContext;
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLImageElement;

import com.interface21.web.servlet.view.xmlc.AbstractXmlcView;
import com.wrox.expertj2ee.ticket.boxoffice.Reservation;
import com.wrox.expertj2ee.ticket.boxoffice.ReservationRequest;
import com.wrox.expertj2ee.ticket.boxoffice.Seat;
import com.wrox.expertj2ee.ticket.boxoffice.support.ReservationImpl;
import com.wrox.expertj2ee.ticket.referencedata.Performance;
import com.wrox.expertj2ee.ticket.referencedata.PriceBand;
import com.wrox.expertj2ee.ticket.referencedata.support.PerformanceImpl;
import com.wrox.expertj2ee.ticket.referencedata.support.PriceBandImpl;
import com.wrox.expertj2ee.ticket.referencedata.support.ShowImpl;
import com.wrox.expertj2ee.ticket.web.TicketController;
import com.wrox.expertj2ee.ticket.web.xmlc.generated.ShowReservationXmlcObject;

public class ShowReservationView extends AbstractXmlcView {

	/**
	 * Constructor for ShowReservationView.
	 */
	public ShowReservationView() {
		super();
	}

	/**
	 * @see AbstractXmlcView#createXMLObject(Map, HttpServletRequest, HttpServletResponse, XMLCContext)
	 */
	protected XMLObject createXMLObject(
		Map model,
		HttpServletRequest request,
		HttpServletResponse response,
		XMLCContext context)
		throws ServletException {
		//ShowReservationXmlcObject showReservationXmlcObject = new ShowReservationXmlcObject();
		ShowReservationXmlcObject showReservationXmlcObject = (ShowReservationXmlcObject) context.getXMLCFactory().create(ShowReservationXmlcObject.class);

		Reservation reservation = (Reservation) model.get(TicketController.RESERVATION_KEY);
		Performance performance = (Performance) model.get(TicketController.PERFORMANCE_KEY);
		PriceBand priceBand = (PriceBand) model.get(TicketController.PRICE_BAND_KEY);

			SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, request.getLocale());
		//SimpleDateFormat df = new SimpleDateFormat();

		df.applyPattern("EEEE MMMM dd, yyyy");
		String formattedDate = df.format(performance.getWhen());
		df.applyPattern("h:mm a");
		String formattedTime = df.format(performance.getWhen());

		NumberFormat cf = NumberFormat.getCurrencyInstance(request.getLocale());
		String formattedTotalPrice = cf.format(reservation.getTotalPrice());
		String formattedBookingFee = cf.format(reservation.getQuoteRequest().getBookingFee());

		// We do this to preserve whatever language comes before actual show title
		showReservationXmlcObject.setTextTitle(
			showReservationXmlcObject.getElementTitle().getText() + " " + performance.getShow().getName());
		showReservationXmlcObject.setTextPerformanceName(performance.getShow().getName());
		showReservationXmlcObject.setTextSeatsRequested("" + reservation.getSeats().length);
		showReservationXmlcObject.setTextSeatType(priceBand.getDescription());
		showReservationXmlcObject.setTextMinutesHeld("" + reservation.getMinutesReservationWillBeValid());
		showReservationXmlcObject.setTextDate(formattedDate);
		showReservationXmlcObject.setTextTime(formattedTime);
		showReservationXmlcObject.setTextTotalPrice(formattedTotalPrice);
		showReservationXmlcObject.setTextBookingFee(formattedBookingFee);

		// Get template node for seat list
		HTMLElement seatEle = showReservationXmlcObject.getElementSeat();
		for (int i = 0; i < reservation.getSeats().length; i++) {
			showReservationXmlcObject.setTextSeat(reservation.getSeats()[i].getName());
			seatEle.getParentNode().insertBefore(seatEle.cloneNode(true), seatEle);
		}
		// Remove template node
		seatEle.getParentNode().removeChild(seatEle);

		if (reservation.getSeatsAreAdjacent()) {
			Element adjacentWarning = showReservationXmlcObject.getElementNonAdjacentWarning();
			adjacentWarning.getParentNode().removeChild(adjacentWarning); 
		} 

		HTMLImageElement graphicEle = (HTMLImageElement) showReservationXmlcObject.getElementSeatingPlanImg();
		graphicEle.setSrc("static/seatingplans/" + performance.getShow().getSeatingPlanId() + ".jpg");
 
		return showReservationXmlcObject;
	}
	

	public static void main(String[] args) {
		try {
			ShowReservationView view = new ShowReservationView();

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

			XMLObject xo = view.createXMLObject(m, null, null, null);
			System.out.println(xo.toDocument());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}