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

	@Override
	public String toString() {
		return name;
	}

	public boolean isLocalHost() {
		// TODO: understand if a configured host name is actually the current
		// host.
		return name.equals(LOCALHOST);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Host && ((Host) o).name.equals(name);
	}
}
