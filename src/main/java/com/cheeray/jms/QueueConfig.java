package com.cheeray.jms;

import java.io.Serializable;

import com.cheeray.jms.annotation.MQueue;

/**
 * Queue definition.
 * 
 * @author Chengwei.Yan
 * 
 */
public class QueueConfig implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String host;
	private final int port;
	private final String channel;
	private final String manager;
	private final String queue;
	private final int threads;

	/**
	 * Constructor.
	 */
	public QueueConfig(MQueue q) {
		this(q.host(), q.port(), q.channel(), q.manager(), q.queue(), q
				.threads());
	}

	public QueueConfig(final String host, final int port, final String channel,
			final String manager, final String queue, final int threads) {
		if (host == null || host.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing host.");
		}
		if (port <= 0) {
			throw new IllegalArgumentException("Missing port.");
		}
		if (channel == null || channel.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing channel.");
		}
		if (manager == null || manager.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing manager.");
		}
		if (queue == null || queue.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing queue.");
		}
		if (threads <= 0) {
			throw new IllegalArgumentException(
					"Invalid threads, minimum 1 by default.");
		}
		this.host = host;
		this.port = port;
		this.channel = channel;
		this.manager = manager;
		this.queue = queue;
		this.threads = threads;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getChannel() {
		return channel;
	}

	public String getManager() {
		return manager;
	}

	public String getQueue() {
		return queue;
	}

	public int getThreads() {
		return threads;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((manager == null) ? 0 : manager.hashCode());
		result = prime * result + port;
		result = prime * result + ((queue == null) ? 0 : queue.hashCode());
		result = prime * result + threads;
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
		QueueConfig other = (QueueConfig) obj;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (manager == null) {
			if (other.manager != null)
				return false;
		} else if (!manager.equals(other.manager))
			return false;
		if (port != other.port)
			return false;
		if (queue == null) {
			if (other.queue != null)
				return false;
		} else if (!queue.equals(other.queue))
			return false;
		if (threads != other.threads)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "QueueDef [host=" + host + ", port=" + port + ", channel="
				+ channel + ", manager=" + manager + ", queue=" + queue
				+ ", threads=" + threads + "]";
	}
}