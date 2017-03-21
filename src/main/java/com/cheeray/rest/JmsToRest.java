package com.cheeray.rest;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cheeray.jms.QueueConfig;
import com.cheeray.mq.MQConsumer;
import com.cheeray.rest.config.JmsConfig;
import com.cheeray.rest.config.RestConfig;

public class JmsToRest {
	private static final Logger LOG = LoggerFactory.getLogger(JmsToRest.class);

	private final List<MQConsumer> jmsConsumers;
	private final RestPusher pusher;

	public JmsToRest(JmsConfig jms, RestConfig rest) throws Exception {
		final String[] queues = jms.getQueues().split(",");
		if (queues.length <= 0) {
			throw new IllegalArgumentException("No queues to consume.");
		}
		this.jmsConsumers = new ArrayList<>(queues.length);
		this.pusher = new RestPusher(rest);
		try {

			Method method = null;
			for (Method m : RestPusher.class.getDeclaredMethods()) {
				if (m.getName().equals("push")) {
					method = m;
				}
			}
			if (method != null) {
				MethodHandle mh = MethodHandles.lookup().unreflect(method);
				for (String q : queues) {
					final QueueConfig cfg = new QueueConfig(jms.getHost(),
							jms.getPort(), jms.getChannel(), jms.getManager(),
							q, 1);
					try {
						for (int i = 0; i < jms.getThreads(); i++) {
							final MQConsumer c = new MQConsumer(pusher, mh,
									cfg, new ArrayList<QueueConfig>());
							this.jmsConsumers.add(c);
						}
					} catch (Exception e) {
						LOG.error("Failed bridge JMS queue: " + q + " to rest "
								+ rest, e);
						throw e;
					}
				}
			} else {
				throw new IllegalArgumentException(
						"Cannot find push method from " + RestPusher.class);
			}
		} catch (Exception e) {
			for (MQConsumer c : jmsConsumers) {
				c.disconnect();
			}
			LOG.error("Failed bridge JMS to rest.", e);
			throw e;
		}
	}

	public void stop() {
		for (MQConsumer c : jmsConsumers) {
			LOG.info("Stop queue consumer.");
			c.down();
		}
	}

	public void start() {
		for (MQConsumer c : jmsConsumers) {
			c.start();
		}
	}

}
