package com.cheeray.mq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cheeray.jms.QueueConfig;
import com.cheeray.jms.QueueMethodHandle;
import com.cheeray.jms.Transporters;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQSimpleConnectionManager;

/**
 * Consume MQ messages and convert them into a string with char(256).
 * 
 * @author Chengwei.Yan
 * 
 */
public class Connectors {
	private static final Logger LOG = LoggerFactory.getLogger(Connectors.class);
	private static final List<MQConsumer> CONSUMERS = new ArrayList<>();

	private static volatile boolean INITED = false;

	private static MQSimpleConnectionManager MQCM;

	public static void init() throws MQException {
		MQCM = new MQSimpleConnectionManager();
		MQCM.setActive(MQSimpleConnectionManager.MODE_ACTIVE);
		MQCM.setTimeout(3600000);
		MQCM.setMaxConnections(75);
		MQCM.setMaxUnusedConnections(50);
		MQEnvironment.setDefaultConnectionManager(MQCM);

		for (QueueMethodHandle qmh : Transporters.CONSUMERS.handles()) {
			for (QueueConfig qc : qmh.getQueues()) {
				for (int i = 0; i < qc.getThreads(); i++) {
					MQConsumer consumer = new MQConsumer(qmh.getInstance(),
							qmh.getMh(), qc, qmh.getBackouts());
					CONSUMERS.add(consumer);
				}
			}
		}
		INITED = true;
	}

	/**
	 * Start all consumers and init them first if yet initialised.
	 * 
	 * @throws MQException
	 */
	public static void start() throws MQException {
		if (!INITED) {
			init();
		}
		for (MQConsumer c : CONSUMERS) {
			try {
				c.start();
			} catch (Exception e) {
				LOG.error("Failed start consumer " + c, e);
			}
		}
	}

	/**
	 * Stop and remove all consumers.
	 */
	public static void stop() {
		final Iterator<MQConsumer> it = CONSUMERS.iterator();
		while (it.hasNext()) {
			it.next().down();
			it.remove();
		}
		INITED = false;
		MQCM.setActive(MQSimpleConnectionManager.MODE_INACTIVE);
	}

	/**
	 * Interrupt consumer threads and wait for value set in system property
	 * JMS_UTIL_WAIT_DURING_SHUTDOWN (default is 5 seconds).
	 * <p>
	 * NOTE: <strong>Only to be used during container shutdown</strong>.
	 * </p>
	 */
	public static void aggressiveStop() {
		for (MQConsumer c: CONSUMERS){
			c.down();
		}
		final int wait = Integer.parseInt(System.getProperty("JMS_UTIL_WAIT_DURING_SHUTDOWN", "5000"));
		try {
			Thread.sleep(wait);
		} catch (Exception ex) {
			// ignore
		}
		final Iterator<MQConsumer> it = CONSUMERS.iterator();
		while (it.hasNext()) {
			final MQConsumer c = it.next();
			if (c.isAlive()) {
				c.interrupt();
			}
			it.remove();
		}
		INITED = false;
		MQCM.setActive(MQSimpleConnectionManager.MODE_INACTIVE);
	}

}
