
package com.wrox.expertj2ee.ticket.web.pdfviews;

import com.interface21.web.servlet.view.pdf.AbstractPdfView;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.List;
import com.wrox.expertj2ee.ticket.boxoffice.Reservation;
import com.wrox.expertj2ee.ticket.referencedata.Performance;
import com.wrox.expertj2ee.ticket.referencedata.PriceBand;
import com.wrox.expertj2ee.ticket.web.TicketController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

public class ShowReservationView extends AbstractPdfView {
	
	private static final String MESSAGE1 = "{0} tickets have been reserved for you for " +
			"{1} minutes to give you time to " + 
			"complete your purchase. The seat numbers are: ";
			
	private static final String COSTING = "The total cost of these tickets will be {0}. " +
			"This includes a booking fee of {1}.";
			
	private static final String ADJACENT_WARNING = "Please note that due of lack of availability, some " +
		" of the seats offered are not adjacent";
	// TODO: 2017/8/25 报错 注释
//	private static final Font HEADLINE_FONT = new Font(Font.TIMES_NEW_ROMAN, 15, Font.BOLD, Color.red);
	
	private static final Font HEADING_FONT = new Font(Font.HELVETICA, 13, Font.ITALIC, Color.black);
	
	private static final Font TEXT_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, Color.black);
	
	private static final Font WARNING_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.black);


	/**
	 * No point in shared date format etc. with XMLC as wouldn't have two of the same
	 * views in the same app
	 */
	protected void buildPdfDocument(Map model, Document pdfDoc, 
			HttpServletRequest request, HttpServletResponse response) throws DocumentException {
		Reservation reservation = (Reservation) model.get(TicketController.RESERVATION_KEY);
		Performance performance = (Performance) model.get(TicketController.PERFORMANCE_KEY);
		PriceBand priceBand = (PriceBand) model.get(TicketController.PRICE_BAND_KEY);
		
		SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, request.getLocale());

		df.applyPattern("EEEE MMMM dd, yyyy");
		String formattedDate = df.format(performance.getWhen());
		df.applyPattern("h:mm a");
		String formattedTime = df.format(performance.getWhen());

		NumberFormat cf = NumberFormat.getCurrencyInstance();
		String formattedTotalPrice = cf.format(reservation.getTotalPrice());
		String formattedBookingFee = cf.format(reservation.getQuoteRequest().getBookingFee());
		
		String title = "Reservation for " + performance.getShow().getName();
//		pdfDoc.add(new Paragraph(title, HEADLINE_FONT));
		
		String when = formattedDate + " at " + formattedTime;
		pdfDoc.add(new Paragraph(when, HEADING_FONT));
		
		pdfDoc.add(new Paragraph());

		
		String note = MessageFormat.format(MESSAGE1, new String[] { "" + reservation.getSeats().length,
			"" + reservation.getMinutesReservationWillBeValid()});
		pdfDoc.add(new Paragraph(note, TEXT_FONT));
		
		
		// Show seats list
		List list = new List(false, 20);
		list.setListSymbol(new Chunk("\u2022", new Font(Font.HELVETICA, 20, Font.BOLD)));
		for (int i = 0; i < reservation.getSeats().length; i++) {
			list.add(new ListItem(reservation.getSeats()[i].getName()));
		}
		pdfDoc.add(list);
		
		
		pdfDoc.add(new Paragraph());
		note = MessageFormat.format(COSTING, new String[] { formattedTotalPrice, formattedBookingFee });
		pdfDoc.add(new Paragraph(note, TEXT_FONT));
		
		
		pdfDoc.add(new Paragraph());
		// Conditional
		if (!reservation.getSeatsAreAdjacent()) {
			pdfDoc.add(new Paragraph(ADJACENT_WARNING, WARNING_FONT));
		}
		
		// Show image...
		//pdfDoc.add(new Image(new )
	}

}
