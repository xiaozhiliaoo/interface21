
package com.wrox.expertj2ee.ticket.referencedata.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java14.java.util.logging.Logger;

import javax.naming.NamingException;
import javax.sql.DataSource;

import com.interface21.beans.factory.InitializingBean;
import com.interface21.jdbc.core.DataSourceUtils;
import com.interface21.jdbc.core.JdbcHelper;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowCallbackHandler;
import com.interface21.jdbc.core.SqlParameter;
import com.interface21.jdbc.object.ManualExtractionSqlQuery;
import com.interface21.jdbc.object.SqlQuery;
import com.wrox.expertj2ee.ticket.referencedata.*;
import com.wrox.expertj2ee.ticket.referencedata.Calendar;
import com.wrox.expertj2ee.ticket.referencedata.Genre;
import com.wrox.expertj2ee.ticket.referencedata.Performance;
import com.wrox.expertj2ee.ticket.referencedata.PriceBand;
import com.wrox.expertj2ee.ticket.referencedata.ReferenceDataException;
import com.wrox.expertj2ee.ticket.referencedata.Show;
import com.wrox.expertj2ee.ticket.referencedata.support.*;
import com.wrox.expertj2ee.ticket.referencedata.support.GenreImpl;
import com.wrox.expertj2ee.ticket.referencedata.support.PerformanceImpl;
import com.wrox.expertj2ee.ticket.referencedata.support.PriceBandImpl;
import com.wrox.expertj2ee.ticket.referencedata.support.ShowImpl;


/**
 * ONLY DATE IS ORACLE SPECIFIC:
 * can be allow overriding,
 * or does it really matter!?
 * NO LOCKING ISSUES IN THIS CLASS
 * 
 * Can't delete 
 * can't change pks
 * BUJT some objects aren't immutable:
 * e.g. could change price bands
 */
public class JdbcCalendar implements Calendar, InitializingBean {
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());

	
	private DataSource dataSource;
	
	//private SqlQuery genreQuery;
	
	private JdbcTemplate jdbcTemplate;
	
	private List genreList;
	
	/** Genre, keyed by Integer id */
	private Map genreHash;
	
	private Map showHash;
	
	private Map performanceHash;
	
	
	/** Seat types, keyed by id */
	private Map seatTypeHash;
	
	/** List of PriceBand objects, keyed by price structure Id */
	private Map priceBandHash;
	
	private SqlQuery priceBandQuery;
	
	private SqlQuery priceStructureIdsQuery;
	
	private SqlQuery allGenreQuery;
	
	private SqlQuery currentGenreQuery;
	
	private SqlQuery seatTypeQuery;
	
	private long lastModified;
	
	/**
	 * Constructor for JdbcCalendar.
	 */
	public JdbcCalendar() throws NamingException {
		this(DataSourceUtils.getDataSourceFromJNDI("ticket-ds"));
	}
	
	/**
	 * Constructor for JdbcCalendar.
	 * Useful for testing.
	 */
	public JdbcCalendar(DataSource ds) {
		this.dataSource = ds;
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.priceBandQuery = new PriceBandQuery();
		this.priceStructureIdsQuery = new PriceStructureIdsQuery();
		// Simply doing a normal join excludes genres without shows
		this.currentGenreQuery = new GenreQuery("select distinct genre.id as id, genre.name as name from genre, show " +
												"where genre.id = show.genre_id");
		this.allGenreQuery = new GenreQuery("select id as id, name as name from genre");
		this.seatTypeQuery = new SeatTypeQuery();
	}
	


	/**
	 * Invoked when this object is loaded into a bean factory.
	 * We use it to refresh our cached data.
	 * @see InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		loadData();
		this.lastModified = System.currentTimeMillis();
	}
	
	public long getLastModified() {
		return this.lastModified;
	}
	
	/**
	 * DO WE ALLOW REFRESH!???? or just substitute a fresh one!?
	 * COULD SUPERCLASS PROTECT THIS!????
	 */
	private void loadData() {
		
		this.genreHash = new HashMap();
		this.showHash = new HashMap();
		this.performanceHash = new HashMap();
		this.seatTypeHash = new HashMap();
		this.priceBandHash = new HashMap();
		
		logger.config("++++ New JdbcCalendar: Loading data...");
		
		this.genreList = currentGenreQuery.execute();
		for (int i = 0; i < genreList.size(); i++) {
			Genre g = (Genre) genreList.get(i);
			genreHash.put(new Integer(g.getId()), g);
		}
		
		
		// Cache all seat types
		List seatTypes = seatTypeQuery.execute();
		for (int i = 0; i < seatTypes.size(); i++) {
			SeatType st = (SeatType) seatTypes.get(i);
			seatTypeHash.put(new Integer(st.getId()), st);
		}
			
		jdbcTemplate.query("select id as id, name as name, genre_id as genre_id, seating_plan_id as spid from show", 
			new RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					Show s = new ShowImpl(rs.getInt("id"), rs.getString("name"), rs.getInt("spid"));
					GenreImpl gi = getGenre(rs.getInt("genre_id"));
					gi.addShow(s);
					showHash.put(new Integer(s.getId()), s);
				}
			});
			
		jdbcTemplate.query("select distinct show.id as show_id, seat_plan_seat.seat_class_id as class_id " +
					"from show, seat_plan_seat " +
					"where seat_plan_seat.seating_plan_id = show.seating_plan_id",
					new RowCallbackHandler() {
						public void processRow(ResultSet rs) throws SQLException {
							int sid = rs.getInt("show_id");
							int cid = rs.getInt("class_id");
							ShowImpl si = (ShowImpl) getShow(sid);
							SeatType st = getSeatType(cid);
							si.addSeatType(st);
				}
			});
			
		// ORACLE SPECIFIC: sysdate and single date
		jdbcTemplate.query("select id as id, show_id as show_id, price_structure_id as price_structure_id, date_and_time as when from performance " +
							"where date_and_time > sysdate " +
							"order by when asc", 
			new RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					int id = rs.getInt("id");
					PerformanceImpl pi = new PerformanceImpl(id, "" + id, rs.getInt("price_structure_id"), rs.getTimestamp("when"));
					ShowImpl si = (ShowImpl) getShow(rs.getInt("show_id"));
					si.addPerformance(pi);
					pi.setShow(si);
					performanceHash.put(new Integer(pi.getId()), pi);
				}
			});

		
		// Get price structure ids so that we can cache 
		// price bands for each.	
		// Save in List of Integer
		List priceStructureIds = priceStructureIdsQuery.execute();
		
		// Retrieve and cache the price bands for each price structure
		// There should never be too many.
		for (int i = 0; i < priceStructureIds.size(); i++) {
			Integer psid = (Integer) priceStructureIds.get(i);
			logger.info("Querying price structure " + psid);
			List priceBands = priceBandQuery.execute(psid.intValue());
			priceBandHash.put(psid, priceBands);
		}
		
		// Set price bands on each performance
		for (Iterator performances = performanceHash.values().iterator(); performances.hasNext(); ) {
			PerformanceImpl pi = (PerformanceImpl) performances.next();
			logger.info("Setting pricebands on performance " + pi.getId());
			List priceBands = (List) priceBandHash.get(new Integer(pi.getPriceStructureId()));
			pi.addPriceBands(priceBands);
		}

		// Make the only publicly exposed collection immutable
		this.genreList = Collections.unmodifiableList(this.genreList);
		
		// Could publish refresh event if aca
	}	// refresh
	
	
	private GenreImpl getGenre(int id) throws ReferenceDataException {
		GenreImpl gi = (GenreImpl) genreHash.get(new Integer(id));
		if (gi == null)
			throw new ReferenceDataException("Internal error in JdbcCalendar: no genre with id=" + id);
		return gi;
	}
	
	private SeatType getSeatType(int id) throws ReferenceDataException {
		SeatType st = (SeatType) seatTypeHash.get(new Integer(id));
		if (st == null)
			throw new ReferenceDataException("Internal error in JdbcCalendar: no SeatType with id=" + id);
		return st;
	}

	/**
	 * @see Calendar#getGenres()
	 */
	public List getCurrentGenres() {
		return genreList;
	}
	
	/**
	 * We don't expect this to be run often enough to bother caching it
	 * @see Calendar#getGenres()
	 */
	public List getAllGenres() {
		return allGenreQuery.execute();
	}

	/**
	 * @see Calendar#getPerformance(int)
	 */
	public Performance getPerformance(int id) throws ReferenceDataException {
		Performance p = (Performance) performanceHash.get(new Integer(id));
		if (p == null)
			throw new ReferenceDataException("Bad request: no performance with id=" + id);
		return p;
	}

	/**
	 * @see Calendar#getShow(int)
	 */
	public Show getShow(int id) {
		Show s = (Show) showHash.get(new Integer(id));
		if (s == null)
			throw new ReferenceDataException("Bad request: no show with id=" + id);
		return s;
	}


	private class GenreQuery extends ManualExtractionSqlQuery {
		public GenreQuery(String sql) {
			super(dataSource, sql);
			compile();
		}
		protected Object extract(ResultSet rs, int rownum) throws SQLException {
			return new GenreImpl(rs.getInt("id"), rs.getString("name"));
		}
	}
	
	
	/**
	 * Return all seat types
	 */
	private class SeatTypeQuery extends ManualExtractionSqlQuery {
		public SeatTypeQuery() {
			super(jdbcTemplate.getDataSource(), 
				"select id as id, code as name, description as description " +
				"from seat_class " +
				"order by id asc");
			compile();
		}

		protected Object extract(ResultSet rs, int rownum) throws SQLException {			
			int pbid = rs.getInt("id");
			String name = rs.getString("name");
			String description = rs.getString("description");
			return new SeatTypeImpl(pbid, name, description);
		}
	}


	/** 
	 * Return price bands for a given price structure
	 */
	private class PriceBandQuery extends ManualExtractionSqlQuery {
		public PriceBandQuery() {
			super(jdbcTemplate.getDataSource(), 
				"select price_band.id as price_band_id, seat_class.id as seat_class_id, " +
				"seat_class.code as name, seat_class.description as description, price_band.price as price " +
				"from price_band, seat_class " +
				"where price_band.price_structure_id = ? " +
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
	
	
	/**
	 * Return all price structure ids
	 */
	private class PriceStructureIdsQuery extends ManualExtractionSqlQuery {
		public PriceStructureIdsQuery() {
			super(jdbcTemplate.getDataSource(), "select id from price_structure");
			compile();
		}

		protected Object extract(ResultSet rs, int rownum) throws SQLException {			
			return new Integer(rs.getInt(1));
		}
	}

}	// class JdbcCalendar
