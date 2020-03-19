
package com.wrox.j2eedd.ticket.referencedata;

/*public class CalendarTestSuite extends TestCase {
	
	DataSource ds = new TestDataSource();
	
	JdbcHelper jh = new JdbcHelper(ds);
	
	Calendar calendar;

	*//**
	 * Constructor for CalendarTestSuite.
	 * @param arg0
	 *//*
	public CalendarTestSuite(String arg0) {
		super(arg0);
	}
	
	

	*//**
	 * @see TestCase#setUp()
	 *//*
	protected void setUp() throws Exception {
		// TAKE FROM BEAN FACTORY
		this.calendar = new JdbcCalendar(ds);
		((InitializingBean) calendar).afterPropertiesSet();
	}
	
	
	
	public void testCurrentGenres() throws Exception {
		List l = calendar.getCurrentGenres();
		
		int[] dbIds = jh.getIDs("select distinct genre_id from show", null);
		assertTrue("genres non null", l != null);
		assertTrue("genres not empty", !l.isEmpty());
		assertTrue("db said count should be " + dbIds.length + ", not " + l.size() + ": genres=[" + StringUtils.collectionToDelimitedString(l, ",") + "]", dbIds.length == l.size());
		
		checkUnique(l, "getCurrentGenres");
		
		for (int i = 0; i < l.size(); i++) {
			Genre g = (Genre) l.get(i);
			System.out.println(g);
			List shows = g.getShows();
			checkUnique(shows, "Genre.getShows");
			assertTrue("Genre shows aren't null", g.getShows() != null);
			assertTrue("Current genres all have shows", !g.getShows().isEmpty());
			for (int j = 0; j < shows.size(); j++) {
				Show s = (Show) shows.get(j);
				testShowFoundByNavigation(s);
			}
		}
	}
	
	private void checkUnique(Collection c, String name) {
		Set s = new HashSet();
		s.addAll(c);
		assertTrue("Cointains duplicates: " + name, s.size () == c.size());
	}
	
	public void testAllGenres() {
		List current = calendar.getCurrentGenres();
		List all = calendar.getAllGenres();
		assertTrue("all is non null", all != null);
		// Breaks if db doesn't have extra genres
		assertTrue("all contains more than curent", all.size() > current.size());
		assertTrue("all contains current", all.containsAll(current));
		for (int i = 0; i < all.size(); i++) {
			Genre g = (Genre) all.get(i);
			if (!current.contains(g)) {
				System.out.println("EMPTY GENRE " + g);
				assertTrue("No shows in non current genre", g.getShows().isEmpty());
			}
		}
	}
	
	public void testBogusShow() {
		try {
			Show s = calendar.getShow(12345);
			fail("Shouldn't have found bogus show");
		}
		catch (ReferenceDataException ex) {
			// correct
		}
	}
	
	public void testBogusPerformance() {
		try {
			Performance p = calendar.getPerformance(666);
			fail("Shouldn't have found bogus performance");
		}
		catch (ReferenceDataException ex) {
			// correct
		}
	}
	
	private void testShowFoundByNavigation(Show s) throws Exception {
		System.out.println(s);
		Show showById = calendar.getShow(s.getId());
		assertTrue("show by id isn't null", showById != null);
		assertTrue("show's seating plan id isn't 0", s.getSeatingPlanId() != 0);
		assertTrue("matches retrieval by id for show " + s.getId(), showById.equals(s));
		List performances = s.getPerformances();
		assertTrue("performances isn't null", performances != null);
		checkUnique(performances, "show.getPerformances");
		// may be empty
		for (int i = 0; i < performances.size(); i++) {
			Performance p = (Performance) performances.get(i);
			
			testPerformanceFoundByNavigation(p);
			Show s2 = p.getShow();
			assertTrue("Show from performance is non null", s2 != null);
			assertTrue("show from performance == ", s2.equals(s));
		}
		
		assertTrue("Show has seat types", !s.getSeatTypes().isEmpty());
		for (int i = 0; i < s.getSeatTypes().size(); i++) {
			SeatType st = (SeatType) s.getSeatTypes().get(i);
			assertTrue("name is non null",st.getName() != null);
			assertTrue("code length = 1 or 2", st.getName().length() > 0 && st.getName().length() <= 2);
			assertTrue("description is longer than code", st.getDescription().length() > 3);
		}
		
		int perfs = jh.runSQLFunction("select count(*) from performance where show_id = " + s.getId() + " and date_and_time > sysdate ");
		assertTrue("matches db performance count of " + perfs + "; wrong=" + s.getPerformances().size(), s.getPerformances().size() == perfs);
	}
	
	private void testPerformanceFoundByNavigation(Performance p) throws Exception {
		System.out.println(p);
		Performance perfById = calendar.getPerformance(p.getId());
		assertTrue("perf by id isn't null", perfById != null);
		assertTrue("matches retrieval by id for perfById " + p.getId(), perfById.equals(p));
		List pbs = p.getPriceBands();
		assertTrue("pricebands aren't null", pbs != null);
		assertTrue("there are price bands", pbs.size() >= 1);
		assertTrue("performance has non null date", p.getWhen() != null);
		assertTrue("performance is in the future", p.getWhen().after(new Date(System.currentTimeMillis())));
		
		matchTypes(p.getShow().getSeatTypes(), p.getPriceBands());
	}
	
	private void matchTypes(List seatTypes, List priceBands) throws Exception {
		assertTrue("seat types length must match price band length", seatTypes.size () == priceBands.size());
		for (int i = 0; i < seatTypes.size(); i++) {
			SeatType st = (SeatType) seatTypes.get(i);
			PriceBand pb = (PriceBand) priceBands.get(i);
			assertTrue("Same type id ", st.getSeatTypeId() == pb.getSeatTypeId());
			assertTrue("same name", st.getName().equals(pb.getName()));
			assertTrue("same description", st.getDescription().equals(pb.getDescription()));
		}
	}

}*/
