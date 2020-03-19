/*

package com.wrox.expertj2ee.ticket.boxoffice.support.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;

import javax.naming.NamingException;
import javax.sql.DataSource;

import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.StructDescriptor;

import com.interface21.jdbc.object.*;
import com.interface21.jdbc.object.ManualExtractionSqlQuery;
import com.interface21.jdbc.object.SqlFunction;
import com.interface21.jdbc.object.SqlQuery;
import com.interface21.jdbc.object.StoredProcedure;
import com.interface21.jdbc.core.*;
import com.interface21.jdbc.core.DataSourceUtils;
import com.interface21.jdbc.core.UncategorizedSQLException;
import com.interface21.jdbc.core.SqlParameter;
import com.wrox.expertj2ee.ticket.boxoffice.BookingPaidForException;
import com.wrox.expertj2ee.ticket.boxoffice.ExpiredReservationTakenException;
import com.wrox.expertj2ee.ticket.boxoffice.Seat;
import com.wrox.expertj2ee.ticket.boxoffice.Reservation;
import com.wrox.expertj2ee.ticket.boxoffice.support.BoxOfficeDAO;
import com.wrox.expertj2ee.ticket.exceptions.FatalException;
import com.wrox.expertj2ee.ticket.referencedata.*;
import com.wrox.expertj2ee.ticket.referencedata.PriceBand;
import com.wrox.expertj2ee.ticket.referencedata.support.PriceBandImpl;


*/
/**
 * Refactor this to support other databases if this is ever relevant.
 * It isn't in the first instance, so we don't need to bother.
 *//*

public class OracleJdbcBoxOfficeDAO implements BoxOfficeDAO {
	
	//-------------------------------------------------------------------------
	// Instance data
	//-------------------------------------------------------------------------
	
	private DataSource ds;
	
	private SqlQuery freeSeatsQuery;
	
	private SqlQuery freeSeatsLockingQuery;
	
	private SqlFunction freeSeatsFunction;
	
	private SeatReserver bookSeats;
	
	private PurchaseRecordCreator purchaseRecordCreator;
	
	private Authorizer authorizer;
	
	private PriceBandQuery priceBandQuery;
	
	
	
	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	*/
/**
	 * Useful for testing
	 *//*

	public OracleJdbcBoxOfficeDAO(DataSource ds) {
		this.ds = ds;
		this.priceBandQuery = new PriceBandQuery();
		freeSeatsQuery = new FreeSeatsInPerformanceOfTypeQuery();
		freeSeatsLockingQuery = new LockingFreeSeatsInPerformanceOfTypeQuery();
		freeSeatsFunction = new SqlFunction(ds, "select count(seat_id) from available_seats where performance_id = ?");
		freeSeatsFunction.declareParameter(new SqlParameter("performance_id", Types.NUMERIC));
		freeSeatsFunction.compile();
		purchaseRecordCreator = new PurchaseRecordCreator(ds);
		bookSeats = new SeatReserver(ds);
		authorizer = new Authorizer(ds);
	}
	
	public OracleJdbcBoxOfficeDAO() throws NamingException {
		this(DataSourceUtils.getDataSourceFromJNDI("ticket-ds"));
	}

	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------
	
	
	
	//---------------------------------------------------------------------
	// Implementation of SeatingPlanDAO
	//---------------------------------------------------------------------
	*/
/**
	 * @see SeatingPlanDataSource#getAllSeats(int)
	 *//*

	public List getAllSeats(int performanceId) {
		return new AllSeatsQuery().execute(performanceId);
	}
	
	public List getPriceBands(int performanceId) {
		return (List) priceBandQuery.execute(performanceId);
	}

	*/
/**
	 * @see SeatingPlanDataSource#getFreeSeats(int, int)
	 *//*

	public List getFreeSeats(int performanceId, int classId, boolean lock) {
		if (lock) {
			return freeSeatsLockingQuery.execute(performanceId, classId);
		}
		else {
			return freeSeatsQuery.execute(performanceId, classId);
		}
	}

	*/
/**
	 * @see SeatingPlanDataSource#getFreeSeatCount(int)
	 *//*

	public int getFreeSeatCount(int performanceId) {
		return freeSeatsFunction.run(performanceId);
	}
	
	
	*/
/**
	 * @see SeatingPlanDataSource#reserve(SeatQuote)
	 *//*

	public String reserve(Reservation quote) {
		// Extract seat ids from quote
		int[] seatIds = new int[quote.getSeats().length];
		for (int i = 0; i < seatIds.length; i++) {
			seatIds[i] = quote.getSeats()[i].getId();
		}
		
		// Call the stored procedure
		Timestamp tsHoldTill = new Timestamp(quote.getQuoteRequest().getHoldTill().getTime());
		String ref = "" + bookSeats.execute(quote.getQuoteRequest().getPerformanceID(), seatIds, quote.getTotalPrice(), tsHoldTill);
		return ref;
	}
	
	
	*/
/**
	 * @see BoxOfficeDAO#getSeatIdsForBooking(String)
	 *//*

	public int[] getSeatIdsForBooking(String bookingId) {
		int bid = Integer.parseInt(bookingId);
		return new JdbcHelper(ds).getIDs("select seat_id from seat_status where booking_id = ? for update",
			new Object[] { new Integer(bid) });
	}
	
	
	*/
/**
	 * Return purchase
	 *//*

	public int createPurchaseRecord(Reservation reservation) throws ExpiredReservationTakenException {
		// Why do we use a string?
		int bookingId =Integer.parseInt(reservation.getReference());
		return purchaseRecordCreator.execute(bookingId);
	}
	
	public Date setAuthorizationCode(int purchaseId, String authorizationCode) {
		
		authorizer.authorize(purchaseId, authorizationCode);
		// A bit cheap...
		return new Date(System.currentTimeMillis());
	}
	
	
	
	//-------------------------------------------------------------------------
	// Implementation methods
	//-------------------------------------------------------------------------
	*/
/**
	 * SQL to return all seats
	 *//*

	private static final String ALL_SEAT_QUERY_SQL = "select seat_id as id, seat.name as name, seat_status.price_band_id as class_id, seat.left as left, seat.right as right " +
	"from seat_status, seat " +
	"where seat_status.performance_id=? " +
	"and seat.id = seat_status.seat_id";
		
	
	*/
/**
	 * Implements extraction logic. Subclasses need to provide constructor to provide
	 * SQL, declare parameters and compile. Can use superclass execute() methods.
	 *//*

	private abstract class SeatQuery extends ManualExtractionSqlQuery {
		
		*/
/**
		 * Convert a row of a ResultSet to a Seat object
		 *//*

		protected Object extract(ResultSet rs, int rownum) throws SQLException {			
			int id = rs.getInt("id");
			String name = rs.getString("name");
			int classId = rs.getInt("class_id");
			int left = rs.getInt("left");
			int right = rs.getInt("right");
			return new Seat(id, name, classId, left, right);
		}
	}
	
	private class AllSeatsQuery extends SeatQuery {
		public AllSeatsQuery() {
			setDataSource(ds);
			setSql(ALL_SEAT_QUERY_SQL);
			declareParameter(new SqlParameter("performance_id", Types.NUMERIC));
			compile();
		}
	}
	
		
	
	private class PriceBandQuery extends ManualExtractionSqlQuery {
		public PriceBandQuery() {
			super(ds, "select price_band.id as price_band_id, seat_class.id as seat_class_id, " +
				"seat_class.code as name, seat_class.description as description, price as price " +
				"from price_band, seat_class " +
				"where price_band.price_structure_id = " +
				"(select price_structure_id from performance where id = ?) " +
				"and price_band.seat_class_id = seat_class.id " +
				"order by price desc");
			declareParameter(new SqlParameter("price_structure_id", Types.NUMERIC));
			compile();
		}
		
		protected Object extract(ResultSet rs, int rownum) throws SQLException {			
			int pbid = rs.getInt("price_band_id");
			int scid = rs.getInt("seat_class_id");
			String name = rs.getString("name");
			String description = rs.getString("description");
			double price = rs.getDouble("price");
			return new PriceBandImpl(pbid, scid, name, description, price);
		}
	}
	
	
	// Uses Oracle 8 outer join syntax and Oracle dates
	// Oracle 9 can use standard syntax
	//private static final String FREE_SEATS_IN_CLASS_QUERY_SQL = 
//	"select fk_seat_id as id " +
//	"from seat_status " +
//	"where seat_status.fk_performance_id = ? " +
//		"and seat_status.fk_price_band_id = ? " + 
//		"and seat_status.fk_booking_id is NULL";
		
		
//		"select fk_seat_id as id " +
//		"from seat_status, booking " +
//		"where seat_status.fk_performance_id = ? " +
//		"and seat_status.fk_price_band_id = ? " +
//		"and booking.authorization_code is null " +
//		"and (booking.reserved_until is null or booking.reserved_until < sysdate) " +
//		"and seat_status.fk_booking_id = booking.booking_id(+)";


	// Uses the view
	private static final String FREE_SEATS_IN_CLASS_QUERY_SQL = 
		"select seat_id as id from available_seats " +
		"where performance_id = ? and price_band_id = ?";
	
	
	*/
/**
	 * Declares parameters and implements extraction logic.
	 * Subclasses need only provide SQL.
	 *//*

	private abstract class AbstractFreeSeatsInPerformanceOfTypeQuery extends ManualExtractionSqlQuery {
		public AbstractFreeSeatsInPerformanceOfTypeQuery(String sql) {
			super(ds, sql);
			declareParameter(new SqlParameter("performance_id", Types.NUMERIC));
			declareParameter(new SqlParameter("price_band_id", Types.NUMERIC));
			compile();
		}
		
		protected Object extract(ResultSet rs, int rownum) throws SQLException {			
			return new Integer(rs.getInt("id"));
		}
	}
	
	private class FreeSeatsInPerformanceOfTypeQuery extends AbstractFreeSeatsInPerformanceOfTypeQuery {
		public FreeSeatsInPerformanceOfTypeQuery() {
			super(FREE_SEATS_IN_CLASS_QUERY_SQL);
		}
	}
	
	*/
/**
	 * Same parameters, but uses "for update" to keep the seats in this class locked
	 *//*

	private class LockingFreeSeatsInPerformanceOfTypeQuery extends AbstractFreeSeatsInPerformanceOfTypeQuery {
		public LockingFreeSeatsInPerformanceOfTypeQuery() {
			super(FREE_SEATS_IN_CLASS_QUERY_SQL + " for update");
		}
	}
	
	
	*/
/**
	 * 
	 * This will only work if the DataSource returns the actual
	 * underlying connection (unlikely in a J2EE server).
	 * Can override for different servers
	 *//*

	protected Connection getOracleConnection(Connection con) throws SQLException {
		return con;
	}
	
	
	private class SeatReserver extends StoredProcedure {

		public SeatReserver(DataSource ds) {
			super(ds, "reserve_seats");
			declareParameter(new SqlParameter("perf_id", Types.INTEGER));
			declareParameter(new SqlParameter("seats", Types.ARRAY));
			declareParameter(new SqlParameter("price", Types.FLOAT));
			declareParameter(new SqlParameter("hold_till", Types.TIMESTAMP));
			declareParameter(new OutputParameter("new_booking_id", Types.INTEGER));
			compile();
		}
		
		*/
/**
		 * @param performanceId id of performance to reserve
		 * @return booking id of new booking
	 	*//*

		public int execute(final int performanceId, final int[] seats, final double price, final Timestamp holdTill) {
			Map out = execute(new StoredProcedure.ParameterMapper() {
				public Map createMap(Connection con) throws SQLException {
					
					con = getOracleConnection(con);
					
					// Types MUST be upper case
					StructDescriptor sd = StructDescriptor.createDescriptor("SEATOBJ", con);
					//an array is Transaction durable (JDBC documentation)
					ArrayDescriptor ad = ArrayDescriptor.createDescriptor("SEAT_RANGE", con);

					Object[] arrayObj = new Object[seats.length];
					for (int i = 0; i < arrayObj.length; i++) {
						arrayObj[i] = new Object[] { new Integer(seats[i])};
						//System.out.println("Will reserve seat with id " + new Integer(seats[i]));
					}

					// Need Con to create object (association with Map)
					ARRAY seatIds = new ARRAY(ad, con, arrayObj);

					Map in = new HashMap();
					
					in.put("perf_id", new Integer(performanceId));
					in.put("seats", seatIds);
					in.put("price", new Float(price));
					// If we use a Date we may lose time information
					in.put("hold_till", holdTill);
					return in;
				}
			});
			Number Id = (Number) out.get("new_booking_id");
			return Id.intValue();
		}
	}	// SeatReserver
	

	
	
	private class PurchaseRecordCreator extends StoredProcedure {

		public PurchaseRecordCreator(DataSource ds) {
			setDataSource(ds);
			setSql("create_purchase_record");
			declareParameter(new SqlParameter("booking_id", Types.INTEGER));
			declareParameter(new OutputParameter("new_purchase_id", Types.INTEGER));
			compile();
		}

		public int execute(int bookingId) {
			
			Map in = new HashMap();
			in.put("booking_id", new Integer(bookingId));
			Map out = execute(in);
			Number Id = (Number) out.get("new_purchase_id");
			return Id.intValue();
		}
	}
	
	private class Authorizer extends SqlUpdate {
		public Authorizer(DataSource ds) {
			setDataSource(ds);
			setSql("update purchase set authorization_code=? where id=?");
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.INTEGER));
			setRequiredRowsAffected(1);
			compile();
		}
		
		public void authorize(int purchaseId, String authorizationCode) {
			update(new Object[] { authorizationCode, new Integer(purchaseId) });
		}
	}

}	// OracleJdbcSeatingPlanDAO
*/
