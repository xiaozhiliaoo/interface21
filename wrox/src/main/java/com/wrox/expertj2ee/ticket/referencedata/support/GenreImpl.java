
package com.wrox.expertj2ee.ticket.referencedata.support;

import java.util.LinkedList;
import java.util.List;

import com.wrox.expertj2ee.ticket.referencedata.Genre;
import com.wrox.expertj2ee.ticket.referencedata.Show;

public class GenreImpl extends AbstractReferenceItem implements Genre {
	
	private List shows = new LinkedList();

	/**
	 * Constructor for GenreImpl.
	 * @param id
	 * @param name
	 */
	public GenreImpl(int id, String name) {
		super(id, name);
	}
	
	
	/**
	 * Concealed from clients
	 */
	public void addShow(Show s) {
		shows.add(s);
	}

	/**
	 * @see Genre#getShows()
	 */
	public List getShows() {
		return shows;
	}

}
