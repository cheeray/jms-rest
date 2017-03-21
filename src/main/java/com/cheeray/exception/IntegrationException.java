package com.cheeray.exception;

public class IntegrationException extends Exception {
	private static final long serialVersionUID = 1L;

	public IntegrationException(String msg) {
		super(msg);
	}

	public IntegrationException(Exception e) {
		super(e);
	}

}
