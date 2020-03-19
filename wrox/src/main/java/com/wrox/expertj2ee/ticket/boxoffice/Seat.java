package com.wrox.expertj2ee.ticket.boxoffice;

import java.io.Serializable;

/**
 * Must be serializable: not for RMI but to go in session
 */
public class Seat implements Serializable {

	private int id;
	private String name;
	private int left;
	private int right;
	private int front;
	private int back;
	private int sclass;

	public Seat(int id, String name, int sclass, int left, int right) {
		this.id = id;
		this.name = name;
		this.sclass = sclass;
		this.left = left;
		this.right = right;
		//			this.front = 0;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getSeatClassId() {
		return sclass;
	}
	public int getLeft() {
		return left;
	}

	public int getRight() {
		return right;
	}

	public String toString() {
		return "Seat: id=" + id + "; name=" + name + "; class=" + sclass + "; l=" + left + "; r=" + right;
	}

}