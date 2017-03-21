package com.cheeray.jms;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.cheeray.jms.annotation.BackoutMode;

/**
 * Handler for an annotated method.
 * <p>
 * Runtime wired with consumer and producer instances.
 * </p>
 * 
 * @author Chengwei.Yan
 * 
 */
public class QueueMethodHandle {
	private final Class<?> type;
	private final MethodHandle mh;
	private final List<QueueConfig> queues;
	private final List<QueueConfig> backouts;
	private final BackoutMode backoutMode;
	/** Runtime target instance to be invoked. */
	private Object instance;

	public QueueMethodHandle(final Class<?> type, final MethodHandle mh,
			final Collection<QueueConfig> queues,
			final Collection<QueueConfig> backouts) {
		this(type, mh, queues, backouts,
				BackoutMode.ANY_FAILURE_STOP_THEN_BACKOUT);
	}

	public QueueMethodHandle(final Class<?> type, final MethodHandle mh,
			final Collection<QueueConfig> queues,
			final Collection<QueueConfig> backouts,
			final BackoutMode backoutMode) {
		if (queues == null || queues.isEmpty()) {
			throw new IllegalArgumentException(
					"At least one queue is required.");
		}
		this.type = type;
		this.mh = mh;
		this.queues = new ArrayList<>(queues);
		this.backouts = new ArrayList<>(backouts);
		this.backoutMode = backoutMode;
	}

	public Class<?> getType() {
		return type;
	}

	public MethodHandle getMh() {
		return mh;
	}

	public List<QueueConfig> getQueues() {
		return Collections.unmodifiableList(queues);
	}

	public List<QueueConfig> getBackouts() {
		return Collections.unmodifiableList(backouts);
	}

	public BackoutMode getBackoutMode() {
		return backoutMode;
	}

	public Object getInstance() {
		return instance;
	}

	public void setInstance(Object instance) {
		this.instance = instance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((backoutMode == null) ? 0 : backoutMode.hashCode());
		result = prime * result
				+ ((backouts == null) ? 0 : backouts.hashCode());
		result = prime * result + ((mh == null) ? 0 : mh.hashCode());
		result = prime * result + ((queues == null) ? 0 : queues.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueueMethodHandle other = (QueueMethodHandle) obj;
		if (backoutMode != other.backoutMode)
			return false;
		if (backouts == null) {
			if (other.backouts != null)
				return false;
		} else if (!backouts.equals(other.backouts))
			return false;
		if (mh == null) {
			if (other.mh != null)
				return false;
		} else if (!mh.equals(other.mh))
			return false;
		if (queues == null) {
			if (other.queues != null)
				return false;
		} else if (!queues.equals(other.queues))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
