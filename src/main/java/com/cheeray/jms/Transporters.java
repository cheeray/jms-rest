package com.cheeray.jms;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Register of discovered consumers and producers.
 * 
 * @author Chengwei.Yan
 * 
 */
public enum Transporters {
	CONSUMERS,
	PRODUCERS;

	/** Map queue configuration to the name of annotated class. */
	private final Map<String, QueueMethodHandle> cfgs = new ConcurrentHashMap<>();

	/**
	 * Add a queue method handle.
	 * 
	 * @param clazz
	 *            The annotated class.
	 * @param methodName
	 *            The name of annotated method.
	 * @param cfg
	 *            The queue and method handle configuration.
	 */
	public void add(Class<?> clazz, String methodName, QueueMethodHandle cfg) {
		cfgs.put(clazz.getName() + "." + methodName, cfg);
	}

	/**
	 * Obtains the queue and method handle.
	 * 
	 * @param clazz
	 *            The annotated class.
	 * @param methodName
	 *            The name of annotated method.
	 * @return a {@link QueueMethodHandle} instance or null if not found.
	 */
	public QueueMethodHandle get(Class<?> clazz, String methodName) {
		return cfgs.get(clazz.getName() + "." + methodName);
	}

	/**
	 * @return all registered {@link QueueMethodHandle}s.
	 */
	public Collection<QueueMethodHandle> handles() {
		return Collections.unmodifiableCollection(cfgs.values());
	}

}
