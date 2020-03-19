
package com.wrox.expertj2ee.ticket.referencedata.support;

import com.wrox.expertj2ee.ticket.referencedata.ReferenceItem;

public abstract class AbstractReferenceItem implements ReferenceItem {
	
	private int id;
	
	private String name;

	/**
	 * Constructor for AbstractReferenceItem.
	 */
	public AbstractReferenceItem(int id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * @see ReferenceItem#getId()
	 */
	public int getId() {
		return id;
	}

	/**
	 * @see ReferenceItem#getName()
	 */
	public String getName() {
		return name;
	}


	/**
	 * Equal if same class and same id
	 */
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!other.getClass().equals(getClass()))
			return false;
		return getId() == ((ReferenceItem) other).getId();
	}

	public String toString() {
		return getClass().getName() + ": id=" + id + "; name='" + name + "'";
	}
}
