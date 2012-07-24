package com.polopoly.ps.ci.exception;

/**
 * Thrown in 10.3 and later when we are running using mvn p:run which means the
 * Polopoly directories do not exist.
 */
public class RunningInMavenException extends CIException {

	public RunningInMavenException(String message) {
		super(message);
	}

}
