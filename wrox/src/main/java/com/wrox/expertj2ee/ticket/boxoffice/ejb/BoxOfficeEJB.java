package com.wrox.expertj2ee.ticket.boxoffice.ejb;

import java.util.*;

import com.interface21.beans.ClassLoaderAnalyzer;
import com.interface21.dao.DataAccessException;
import com.interface21.ejb.support.AbstractStatelessSessionBean;
import com.wrox.expertj2ee.ticket.boxoffice.*;
import com.wrox.expertj2ee.ticket.boxoffice.support.*;
import com.wrox.expertj2ee.ticket.exceptions.NoSuchPerformanceException;
import com.wrox.expertj2ee.ticket.referencedata.PriceBand;

public class BoxOfficeEJB extends AbstractStatelessSessionBean implements BoxOffice {

	/** PerformanceSeatingPlan, keyed by Integer performance Id */
	private Map performanceSeatingPlanHash = new HashMap();

	private BoxOfficeDAO dao;

	private CreditCardProcessor creditCardProcessor;

	/**
	 * @see AbstractStatelessSessionBean#ejbCreate()
	 */
	public void ejbCreate() {
		logger.config("Trying to load data source bean");
		this.dao = (BoxOfficeDAO) getBeanFactory().getBean("dao");
		logger.config("Data source loaded OK: [" + this.dao + "]");

		this.creditCardProcessor = (CreditCardProcessor) getBeanFactory().getBean("creditCardProcessor");
		logger.config("Credit card processing loaded OK: [" + this.creditCardProcessor + "]");
	}

	/**
	 * @see BoxOffice#getPriceBands(int)
	 * NO SUCH PERFORMANCE!?
	 */
	public PriceBand[] getPriceBands(int performanceId) throws NoSuchPerformanceException {
		PerformanceSeatingPlan psp = getPerformanceSeatingPlan(performanceId);
		return psp.getPriceBands();
	}

	/**
	 * @see BoxOffice#getSeatCount(int)
	 */
	public int getSeatCount(int performanceId) throws NoSuchPerformanceException {
		PerformanceSeatingPlan psp = getPerformanceSeatingPlan(performanceId);
		return psp.getSeatCount();
	}
	
	

	/**
	 * @see BoxOffice#getSeatCount(int)
	 */
	public int getFreeSeatCount(int performanceId) throws NoSuchPerformanceException {
		
		String s = ClassLoaderAnalyzer.showClassLoaderHierarchy(this, "ticketcontroller", "\n", "\t"  );
		System.out.println("EJB.getFreeSeatCount() context loader " + s);
		logger.warning(s);
		
		PerformanceSeatingPlan psp = getPerformanceSeatingPlan(performanceId);
		return psp.getFreeSeatCount();
	}
	
	public int getFreeSeatCount(int performanceId, int seatTypeId) throws NoSuchPerformanceException {
		PerformanceSeatingPlan psp = getPerformanceSeatingPlan(performanceId);
		return psp.getFreeSeatCount(seatTypeId);
	}
	
//	public PerformanceWithAvailability getPerformanceWithAvailability(int performanceId) throws NoSuchPerformanceException {
//		PerformanceSeatingPlan psp = getPerformanceSeatingPlan(performanceId);
//		return psp.getAvailability();
//	}

	/**
	 * @see BoxOffice#allocateSeats(SeatQuoteRequest)
	 */
	public Reservation allocateSeats(ReservationRequest request)
		throws NoSuchPerformanceException, NotEnoughSeatsException, InvalidSeatingRequestException {
		PerformanceSeatingPlan psp = null;

		psp = getPerformanceSeatingPlan(request.getPerformanceID());

		// if reserve, lock...
		SeatTypeAvailability st = psp.newSeatingStatus(request.getClassID(), request.isReserve());
		// May throw not enough seats exception here
		SeatAllocation sa = st.allocateSeats(request.getSeatsRequested(), request.getSeatsMustBeAdjacent());

		double unitPrice = psp.getPriceOfSeatType(request.getClassID());
		double price = request.getBookingFee() + unitPrice * request.getSeatsRequested();

		ReservationImpl quote = new ReservationImpl(request, sa.getSeats(), sa.adjacent(), price, null);

		if (request.isReserve()) {
			logger.info("Making reservation");
			quote.reserve(dao.reserve(quote));
		}

		logger.info("Quoted OK: [" + quote + "]");
		return quote;
	}

	/**
	 * Return data or null
	 * We don't need to worry about synchronization in the EJB tier
	 */
	private PerformanceSeatingPlan getPerformanceSeatingPlan(int performanceId) throws NoSuchPerformanceException {
		Integer key = new Integer(performanceId);
		PerformanceSeatingPlan psp = (PerformanceSeatingPlan) performanceSeatingPlanHash.get(key);
		if (psp == null) {
			psp = new DefaultPerformanceSeatingPlan(performanceId, dao);
			logger.info("Caching PerformanceSeatingPlan [" + psp + "]");
			performanceSeatingPlanHash.put(key, psp);
		}
		return psp;
	}

	/**
	 * @see BoxOffice#reserve(String, String, Reservation)
	 */
	public Booking confirmReservation(PurchaseRequest purchase)
		throws
			ExpiredReservationTakenException,
			CreditCardAuthorizationException,
			BoxOfficeInternalException,
			InvalidSeatingRequestException {

		if (!purchase.getReservation().isReserved())
			throw new InvalidSeatingRequestException("No reservation");

		// Don't waste time.
		// This may throw us out of here with an InvalidCreditCardNumber exception
		creditCardProcessor.validate(purchase.getCardNumber(), purchase.getExpiry());

		logger.info("Checking seats haven't been taken");
		int[] ids = dao.getSeatIdsForBooking(purchase.getReservation().getReference());
		if (!checkIdsMatch(purchase.getReservation(), ids)) {
			//			if (quote.getTimestamp())
			// IF EXPIRED
			throw new ExpiredReservationTakenException(
				"Seats gone: reservation expected " + purchase.getReservation().getSeats().length + "; only " + ids.length + " remain");

			//KILL RESERVATION
		}

		// First create authorization, so the user will never have
		// their card debited without getting seats.
		// Will show with pending.
		int purchaseId = dao.createPurchaseRecord(purchase.getReservation());
		String authorization = null;

		try {
			authorization = creditCardProcessor.process(purchase.getReservation().getTotalPrice(), purchase.getCardNumber(), purchase.getExpiry());
			
			// Complete data update for purchase if the 
			// credit card debity succeeded
			dao.setAuthorizationCode(purchaseId, authorization);

			Booking booking = new ImmutableBooking(authorization, new Date(), purchase.getReservation());
			logger.info("Booking successful [" + booking + "]");
			return booking;
		}
		catch (CreditCardAuthorizationException ex) {
			logger.severe("Failed to authorize credit card number");
			getSessionContext().setRollbackOnly();
			throw ex;
		}
		catch (DataAccessException ex) {
			// Failed to authorize: we know we've created pending purchase record
			// DO NOT ROLLBACK: this will lose PENDING record
			// This is unusual: of course the tx may not commit, so we want to log 
			// as much information as possible as well
			String mesg ="**** Database problem: failed to set authorization code '"
					+ authorization + "' for purchase id " + purchaseId
					+ "; payment WAS accepted for reservation ["
					+ purchase.getReservation() + "] ****";
			logger.severe(mesg);
			// Must throw a checked exception to avoid automatic rollback 
			throw new BoxOfficeInternalException(mesg);
		}
	}

	/**
	 * Check that the ids in this Reservation match ids from the
	 * database
	 */
	private boolean checkIdsMatch(Reservation r, int[] ids) {
		List idslist = new LinkedList();
		for (int i = 0; i < ids.length; i++)
			idslist.add(new Integer(ids[i]));
		for (int i = 0; i < r.getSeats().length; i++) {
			Integer seatId = new Integer(r.getSeats()[i].getId());
			if (!idslist.contains(seatId))
				return false;
		}
		return true;
	}

} // BoxOfficeEJB