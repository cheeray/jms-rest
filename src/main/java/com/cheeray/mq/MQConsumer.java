package com.cheeray.mq;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cheeray.exception.CollectionException;
import com.cheeray.jms.QueueConfig;
import com.cheeray.jms.annotation.BackoutMode;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPoolToken;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQHeader;
import com.ibm.mq.headers.MQHeaderIterator;

/**
 * Consume MQ messages and convert them into a string with char(256).
 * 
 * @author Chengwei.Yan
 * 
 */
public class MQConsumer extends Thread {
	private static final Logger LOG = LoggerFactory.getLogger(MQConsumer.class);

	private volatile boolean running = false;

	private final Object receiver;
	private final MethodHandle mh;
	private final QueueConfig cfg;
	private final Collection<QueueConfig> backouts;
	private MQPoolToken token;
	private MQQueueManager qm;
	private MQQueue queue;
	private static final AtomicInteger IDX = new AtomicInteger(0);

	public MQConsumer(Object receiver, MethodHandle mh, QueueConfig cfg, Collection<QueueConfig> backouts)
			throws MQException {
		this.receiver = receiver;
		this.mh = mh;
		this.cfg = cfg;
		this.backouts = backouts;
		this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LOG.error("Consumer {} was termnated due to {}.", t.getName(), e);
				t.getThreadGroup().uncaughtException(t, e);
			}

		});
		connect();
	}

	@Override
	public void run() {
		this.running = true;
		Thread.currentThread().setName(receiver.getClass().getSimpleName() + ":" + cfg.getHost() + ":" + cfg.getQueue()
				+ ':' + IDX.incrementAndGet());
		final int wait = Integer.parseInt(System.getProperty("JMS_UTIL_WAIT_INTERVAL", "1000"));
		final int sleep = Integer.parseInt(System.getProperty("JMS_UTIL_SLEEP_INTERVAL", "100"));
		MQException.logExclude(CMQC.MQRC_NO_MSG_AVAILABLE);
		while (running) {
			try {
				if (qm == null || !qm.isConnected()) {
					reconnect();
				}

				final MQMessage message = new MQMessage();
				final MQGetMessageOptions gmo = new MQGetMessageOptions();
				gmo.options = CMQC.MQGMO_WAIT | CMQC.MQGMO_PROPERTIES_AS_Q_DEF | CMQC.MQGMO_FAIL_IF_QUIESCING;
				gmo.matchOptions = CMQC.MQMO_NONE;
				gmo.waitInterval = wait > 0 ? wait : CMQC.MQWI_UNLIMITED;

				queue.get(message, gmo);

				final String msgId = message.getStringProperty(MQConstants.MQ_JMS_MESSAGE_ID);
				LOG.info("Read message[{}].", msgId);
				final MQHeaderIterator it = new MQHeaderIterator(message);
				String charset = "UTF-8";
				while (it.hasNext()) {
					final MQHeader h = it.nextHeader();
					// FIXME: Read charset from header ...
					for (Object fo : h.fields()) {
						final MQHeader.Field f = MQHeader.Field.class.cast(fo);
						LOG.debug("Header {} : {} : {}.", f.getName(), f.getType(), f.getValue());
					}
				}

				final byte[] data = it.getBodyAsBytes();
				if (data.length > 0) {
					final String text = new String(data, charset);
					if (LOG.isDebugEnabled()) {
						LOG.debug("Message[{}]:{}.", msgId, text);
					}
					try {
						Object result = null;
						switch (mh.type().parameterCount()) {
						case 2:
							result = mh.invoke(receiver, text);
							break;
						case 3:
							result = mh.invoke(receiver, data, msgId);
							break;
						case 4:
							result = mh.invoke(receiver, data, msgId, getUsrProperties(message));
							break;
						default:
							throw new IllegalArgumentException("Invalid number of parameters for consumer " + receiver);
						}
						if (result != null && result instanceof Future) {
							Future.class.cast(result).get();
						}
					} catch (Throwable e) {
						LOG.error("Failed process message:" + msgId, e);
						if (backouts != null && !backouts.isEmpty()) {
							try {
								backout(data);
							} catch (CollectionException e1) {
								LOG.error("Backout failed:" + msgId, e);
								LOG.error("Please manually recover message {}: {}.", msgId, text);
							}
						} else {
							// TODO: report an error.
							// queue.putReportMessage(message);
							LOG.error("No backout for message {}: {}.", msgId, text);
						}
					}
				} else {
					LOG.error("No data received for message:{}.", msgId);
				}

			} catch (MQException e) {
				if (e.getReason() != 2033) {
					LOG.error("Faild reading message.", e);
					reconnect();
				}
			} catch (Exception e) {
				LOG.error("Faild reading message.", e);
				reconnect();
			}

			// Wait for another message ...
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				LOG.error("Consumer was interrupted.", e);
				this.running = false;
			}
		}
		LOG.error("Consumer: {} stopped.", cfg);
		disconnect();
	}

	/**
	 * Fetch user prperties from a message.
	 */
	private static Map<String, String> getUsrProperties(final MQMessage message) {
		final Map<String, String> headerMap = new HashMap<>();
		try {
			final Enumeration<String> properties = message.getPropertyNames("%");
			while (properties.hasMoreElements()) {
				final String property = properties.nextElement();
				headerMap.put(property, message.getStringProperty(property));
			}
		} catch (MQException e) {
			LOG.error("Failed reading user properties.", e);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("HeaderMap[{}]", headerMap);
		}
		return headerMap;
	}

	/**
	 * Reconnect to MQ server.
	 */
	private void reconnect() {
		try {
			disconnect();
		} catch (Exception e) {
			LOG.error("Failed disconnect consumer: {}.", cfg);
		}
		try {
			connect();
		} catch (Exception ex) {
			LOG.error("Failed reconnect.", ex);
			// Failed reconnect, delay next retry ...
			try {
				final long duration = Long.parseLong(System.getProperty("JMS_UTIL_RETRY_INTERVAL", "10000"));
				LOG.info("Retry after {} seconds.", TimeUnit.MILLISECONDS.toSeconds(duration));
				Thread.sleep(duration);
			} catch (Exception e) {
				LOG.error("Consumer reconnection was interrupted.", e);
			}
		}
	}

	/**
	 * Backout the message.
	 * 
	 * @param payload
	 *            The payload of message.
	 * @throws MQException
	 * @throws IOException
	 * @throws CollectionException
	 */
	private void backout(byte[] payload) throws MQException, IOException, CollectionException {
		MQProducer.send(backouts, new ArrayList<QueueConfig>(), BackoutMode.ANY_FAILURE_STOP_THEN_BACKOUT, payload);
	}

	/**
	 * Try connect to MQ.
	 * 
	 * @param qmh
	 * @throws MQException
	 */
	private void connect() throws MQException {
		LOG.info("Connect consumer: {}.", cfg);
		this.token = MQEnvironment.addConnectionPoolToken();
		try {
			this.qm = new MQQueueManager(cfg.getManager(), MQUtil.toProperties(cfg));
			try {
				this.queue = qm.accessQueue(cfg.getQueue(), CMQC.MQOO_INPUT_AS_Q_DEF);
			} catch (MQException e) {
				qm.disconnect();
				LOG.error("Failed access inbound queue {}.", cfg.getQueue());
				throw e;
			}
		} catch (MQException e) {
			LOG.error("Faild create queue manager with configuration: {}.", cfg);
			throw e;
		} finally {
			MQEnvironment.removeConnectionPoolToken(token);
		}
	}

	/**
	 * Disconnect from MQ.
	 */
	public void disconnect() {
		LOG.info("Disconnect consumer: {}.", cfg);
		if (queue != null && queue.isOpen()) {
			try {
				queue.close();
			} catch (Exception e) {
				LOG.error("Failed closing queue.", e);
			}
		}
		if (qm != null && qm.isConnected()) {
			try {
				qm.disconnect();
			} catch (Exception e) {
				LOG.error("Failed close queue manager.", e);
			}
		}
		if (token != null) {
			MQEnvironment.removeConnectionPoolToken(token);
		}
	}

	/**
	 * Shutdown the consumer.
	 */
	public void down() {
		LOG.info("Consumer: {} is shutting down.", cfg);
		running = false;
	}

}
