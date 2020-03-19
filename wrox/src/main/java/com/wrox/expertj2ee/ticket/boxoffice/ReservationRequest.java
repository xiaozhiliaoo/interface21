
package com.wrox.expertj2ee.ticket.boxoffice;

import java.io.Serializable;
import java.util.Date;

public class ReservationRequest implements Serializable {
	
	private static final long MILLIS_PER_MINUTE = 1000L * 60L;

	//-------------------------------------------------------------------------
	// Instance data
	//-------------------------------------------------------------------------
	/** Holds value of property performanceID. */
	private int performanceID;
	
	/** Holds value of property seatsRequested. */
	private int seatsRequested;
	
	/** Holds value of property bookingFee. */
	private double bookingFee;
	
	/** Holds value of property classID. */
	private int classID;
	
	/** Holds value of property reserve. */
	private boolean reserve;
	
	private Date holdTill;
	
	private boolean mustBeAdjacent;
	
	//-------------------------------------------------------------------------
	// Contructors
	//-------------------------------------------------------------------------
	/** Creates new QuoteRequest */
    public ReservationRequest() {
    }


	/** Creates new QuoteRequest, setting all fields
	 */
    public ReservationRequest(int performanceID, int classID, int seatsRequested, boolean reserve, double bookingFee, int minutesToHold) throws InvalidSeatingRequestException  {
		this.performanceID = performanceID;
		this.classID = classID;
		this.seatsRequested = seatsRequested;
		this.reserve = reserve;
		this.bookingFee = bookingFee;
		holdFor(minutesToHold);
    }
	
	//-------------------------------------------------------------------------
	// Bean properties
	//-------------------------------------------------------------------------
	/** Getter for property performanceID.
	 * @return Value of property performanceID.
	 */
	public int getPerformanceID() {
		return performanceID;
	}
	
	/** Setter for property performanceID.
	 * @param performanceID New value of property performanceID.
	 */
	public void setPerformanceID(int performanceID) {
		this.performanceID = performanceID;
	}
	
	/** Getter for property seatsRequested.
	 * @return Value of property seatsRequested.
	 */
	public int getSeatsRequested() {
		return seatsRequested;
	}
	
	/** Setter for property seatsRequested.
	 * @param seatsRequested New value of property seatsRequested.
	 */
	public void setSeatsRequested(int seatsRequested) {
		this.seatsRequested = seatsRequested;
	}
	
	
	public boolean getSeatsMustBeAdjacent() {
		return mustBeAdjacent;
	}
	
	public void setSeatsMustBeAdjacent(boolean mustBeAdjacent) {
		this.mustBeAdjacent = mustBeAdjacent;
	}
	
	
	/** Getter for property bookingFee.
	 * @return Value of property bookingFee.
	 */
	public double getBookingFee() {
		return bookingFee;
	}
	
	/** Setter for property bookingFee.
	 * @param bookingFee New value of property bookingFee.
	 */
	public void setBookingFee(double bookingFee) {
		this.bookingFee = bookingFee;
	}
	
	public void holdFor(int minutes) throws InvalidSeatingRequestException {
		if (holdTill != null)
			throw new InvalidSeatingRequestException("holdFor is immutable: cannot reset");
		holdTill = new Date(System.currentTimeMillis() + minutes * MILLIS_PER_MINUTE);
		
	}
	
	public Date getHoldTill() {
		//if (holdTill == null)
		//	throw new InvalidSeatingRequestException("Must set how long to hold reservation for");
		return holdTill;
	}
	
	/** Getter for property classID.
	 * @return Value of property classID.
	 */
	public int getClassID() {
		return classID;
	}
	
	/** Setter for property classID.
	 * @param classID New value of property classID.
	 */
	public void setClassID(int classID) {
		this.classID = classID;
	}
	
	/** Getter for property reserve.
	 * @return Value of property reserve.
	 */
	public boolean isReserve() {
		return reserve;
	}
	
	/** Setter for property reserve.
	 * @param reserve New value of property reserve.
	 */
	public void setReserve(boolean reserve) {
		this.reserve = reserve;
	}
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer("Seat quote request: ");;
		sb.append("performanceID=" + performanceID + "; ");
		sb.append("classID=" + classID + "; ");
		sb.append("reserve=" + reserve + "; ");
		sb.append("held till " + holdTill + "; ");
		sb.append("seatsRequested=" + seatsRequested + "; ");
		sb.append("mustBeAdjacent=" + mustBeAdjacent + "; ");
		sb.append("bookingFee=" + bookingFee + "; ");
		return sb.toString();
	}

}
