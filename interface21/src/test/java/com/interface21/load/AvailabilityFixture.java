package com.interface21.load;/*
package com.interface21.load;

import java.util.LinkedList;
import java.util.List;

import jdbc.*;
import jdbc.TestDataSource;


import com.wrox.expertj2ee.ticket.boxoffice.*;
import com.wrox.expertj2ee.ticket.command.support.CachingAvailabilityCheck;
import com.wrox.expertj2ee.ticket.exceptions.NoSuchPerformanceException;
import com.wrox.expertj2ee.ticket.referencedata.jdbc.JdbcCalendar;

public class AvailabilityFixture {

	public final CachingAvailabilityCheck availabilityCheck;

	public final List shows = new LinkedList();

	private long timeout;

	private JdbcCalendar calendar = null;

	private long minDelay;
	private long maxDelay;

	public AvailabilityFixture() throws Exception {
		calendar = new JdbcCalendar(new TestDataSource());
		calendar.afterPropertiesSet();
		shows.add(calendar.getShow(1));

		availabilityCheck = new CachingAvailabilityCheck();
		availabilityCheck.setCalendar(calendar);
		availabilityCheck.setBoxOffice(new DummyBoxOffice());
		
		System.out.println("Initialized fixture...");
	}

	public void setTimeout(int timeout) {
		availabilityCheck.setTimeout(timeout);
	}

	public void setMinDelay(long minDelay) {
		this.minDelay = minDelay;
	}

	public void setMaxDelay(long maxDelay) {
		this.maxDelay = maxDelay;
	}

	private class DummyBoxOffice implements BoxOffice {
		*/
/**
		 * @see BoxOffice#allocateSeats(ReservationRequest)
		 *//*

		public Reservation allocateSeats(ReservationRequest request)
			throws NotEnoughSeatsException, NoSuchPerformanceException, InvalidSeatingRequestException {
			throw new UnsupportedOperationException("DummyBoxOffice.allocateSeats");
		}

		*/
/**
		 * @see BoxOffice#confirmReservation(PurchaseRequest)
		 *//*

		public Booking confirmReservation(PurchaseRequest purchase)
			throws
				ExpiredReservationTakenException,
				CreditCardAuthorizationException,
				InvalidSeatingRequestException,
				BoxOfficeInternalException {
			throw new UnsupportedOperationException("DummyBoxOffice.confirmReservation");
		}

		*/
/**
		 * @see BoxOffice#getFreeSeatCount(int, int)
		 *//*

		public int getFreeSeatCount(int performanceId, int seatTypeId) throws NoSuchPerformanceException {
			AbstractTest.simulateDelay(minDelay, maxDelay);
			return 10;
		}

		*/
/**
		 * @see BoxOffice#getFreeSeatCount(int)
		 *//*

		public int getFreeSeatCount(int performanceId) throws NoSuchPerformanceException {
			AbstractTest.simulateDelay(minDelay, maxDelay);
			return 30;
		}

		*/
/**
		 * @see BoxOffice#getSeatCount(int)
		 *//*

		public int getSeatCount(int performanceId) throws NoSuchPerformanceException {
			//simulateDelay(minDelay, maxDelay);
			return 200;
		}

	}

}*/
