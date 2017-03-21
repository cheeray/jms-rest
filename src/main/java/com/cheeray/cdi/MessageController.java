package com.cheeray.cdi;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cheeray.mq.Connectors;

@ApplicationScoped
public class MessageController {
	private static final Logger LOG = LoggerFactory.getLogger(MessageController.class);

	public void onMessage(@Observes SemaphoreEvent e) {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("On semaphore event: {}.", e);
			}
			if (e.isSwitchOn()) {
				Connectors.start();
			} else {
				Connectors.stop();
			}
		} catch (Exception ex) {
			LOG.error("Failed dispatch " + e, ex);
		}
	}

	@PreDestroy
	public void cleanup() {
		LOG.info("Stop all JMS connectors during container shutdown.");
		Connectors.aggressiveStop();
	}

}
