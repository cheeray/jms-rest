package com.cheeray.cdi;

import java.io.Serializable;

/**
 * Switch on/off message consumers and producers.
 * 
 * @author Chengwei.Yan
 * 
 */
public class SemaphoreEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private final boolean switchOn;

	public SemaphoreEvent(final boolean switchOn) {
		this.switchOn = switchOn;
	}

	public boolean isSwitchOn() {
		return switchOn;
	}
}
