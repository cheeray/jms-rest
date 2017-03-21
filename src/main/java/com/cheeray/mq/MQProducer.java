package com.cheeray.mq;

import static com.ibm.mq.constants.CMQC.MQCI_NONE;
import static com.ibm.mq.constants.CMQC.MQFMT_STRING;
import static com.ibm.mq.constants.CMQC.MQMI_NONE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cheeray.exception.CollectionException;
import com.cheeray.jms.QueueConfig;
import com.cheeray.jms.annotation.BackoutMode;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPoolToken;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;

/**
 * Produce MQ messages.
 * 
 * @author Chengwei.Yan
 * 
 */
public class MQProducer {
	private static final Logger LOG = LoggerFactory.getLogger(MQProducer.class);

	public static void send(Collection<QueueConfig> queues, Collection<QueueConfig> backouts, BackoutMode mode,
			byte[] payload) throws MQException, IOException, CollectionException {
		send(queues, backouts, mode, payload, null);
	}

	/**
	 * Send a message to a collection of queues and try alternative queues if
	 * failed.
	 * 
	 * @param queues
	 *            The queues.
	 * @param backouts
	 *            The alternative queues.
	 * @param mode
	 *            The backout mode.
	 * @param payload
	 *            The message payload.
	 * @throws MQException
	 * @throws IOException
	 * @throws CollectionException
	 */
	public static void send(Collection<QueueConfig> queues, Collection<QueueConfig> backouts, BackoutMode mode,
			byte[] payload, MsgIdListener msgIdListener) throws MQException, IOException, CollectionException {
		if (payload != null) {
			MQMessage message = new MQMessage();
			message.format = MQFMT_STRING;
			message.clearMessage();
			message.messageId = MQMI_NONE;
			message.correlationId = MQCI_NONE;
			message.write(payload);

			boolean sent = false;
			boolean anyFailure = false;
			boolean backout = false;
			final List<MQException> exs = new ArrayList<>();
			for (QueueConfig q : queues) {
				try {
					send(q, message);
					sent = true;
					String msgId = message.getStringProperty(MQConstants.MQ_JMS_MESSAGE_ID);
					LOG.info("Put message[{}] successfully.", msgId);
					if (msgIdListener != null) {
						msgIdListener.on(q.getQueue(), msgId);
					}
				} catch (MQException e) {
					LOG.error("Failed sending message to " + q, e);
					if (mode == BackoutMode.ANY_FAILURE_STOP_THEN_BACKOUT) {
						backout = true;
						break;
					}
					anyFailure = true;
					exs.add(e);
				}
			}

			// If not send, send via backout ...
			if (backout || !sent && mode == BackoutMode.EACH_TRIED_ALL_FAILED
					|| anyFailure && mode == BackoutMode.EACH_TRIED_ANY_FAILURE) {
				if (!backouts.isEmpty()) {
					LOG.info("Faild sending message, try backout ...");
					send(backouts, message, msgIdListener);
				} else {
					throw new CollectionException(exs);
				}
			}

		}
	}

	/**
	 * Send a message to a collection of queues.
	 * 
	 * @param cfgs
	 *            The queue configurations.
	 * @param message
	 *            The message.
	 * @throws MQException
	 */
	private static void send(Collection<QueueConfig> cfgs, MQMessage message, MsgIdListener msgIdListener) throws MQException {
		for (QueueConfig cfg : cfgs) {
			send(cfg, message);
			String msgId = message.getStringProperty(MQConstants.MQ_JMS_MESSAGE_ID);
			if (msgIdListener != null) {
				msgIdListener.on(cfg.getQueue(), msgId);
			}
		}
	}

	/**
	 * Send a message to a queue.
	 * 
	 * @param cfg
	 *            The queue configuration.
	 * @param message
	 *            The message.
	 * @throws MQException
	 */
	private static void send(QueueConfig cfg, MQMessage message) throws MQException {
		final MQPoolToken token = MQEnvironment.addConnectionPoolToken();
		try {
			final MQQueueManager qm = new MQQueueManager(cfg.getManager(), MQUtil.toProperties(cfg));
			try {
				final MQQueue q = qm.accessQueue(cfg.getQueue(), CMQC.MQOO_OUTPUT);
				try {
					q.put(message);
				} finally {
					q.close();
				}
			} finally {
				qm.disconnect();
			}
		} catch (MQException e) {
			LOG.error("Failed sending message.", e);
			throw e;
		} finally {
			MQEnvironment.removeConnectionPoolToken(token);
		}
	}
}
