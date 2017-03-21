package com.cheeray.exception;

import java.util.Collection;

public class CollectionException extends Exception {
	private static final long serialVersionUID = 1L;
	private final Collection<? extends Throwable> exceptions;

	public CollectionException(Collection<? extends Throwable> exs) {
		this.exceptions = exs;
	}

	public Collection<? extends Throwable> getExceptions() {
		return exceptions;
	}

}
