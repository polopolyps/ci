package com.polopoly.ps.ci;

public class Host {
	private static final String LOCALHOST = "localhost";
	private String name;

	public Host() {
		this(LOCALHOST);
	}
	
	public Host(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	
	public boolean isLocalHost() {
		return name.equals(LOCALHOST);
	}

	public boolean equals(Object o) {
		return o instanceof Host && ((Host) o).name.equals(name);
	}
}
