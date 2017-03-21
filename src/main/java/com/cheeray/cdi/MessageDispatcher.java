package com.cheeray.cdi;

import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cheeray.jms.QueueMethodHandle;
import com.cheeray.jms.Transporters;
import com.cheeray.mq.DefaultMsgIdListener;
import com.cheeray.mq.MQProducer;

public class MessageDispatcher {
	private static final Logger LOG = LoggerFactory
			.getLogger(MessageDispatcher.class);

	public void onMessage(@Observes DispatchEvent e) {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("On dispatch event: {}.", e);
			}
			final QueueMethodHandle qmh = Transporters.PRODUCERS.get(
					e.getSourceType(), e.getMethodName());
			if (qmh != null) {
				final DefaultMsgIdListener listener = new DefaultMsgIdListener();
				MQProducer.send(qmh.getQueues(), qmh.getBackouts(),
						qmh.getBackoutMode(), e.getPayload().getBytes("UTF-8"), listener);
				e.setMsgId(listener.getMsgId());
			} else {
				LOG.error("Missing @MProducer annotation on method {}.{}.",
						e.getClass(), e.getMethodName());
			}
		} catch (Exception ex) {
			LOG.error("Failed dispatch " + e, ex);
		}
	}

}
