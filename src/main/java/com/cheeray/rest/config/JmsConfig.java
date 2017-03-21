package com.cheeray.rest.config;

public class JmsConfig {
	private final String host;
	private final int port;
	private final String channel;
	private final String manager;
	private final String queues;
	private final int threads;

	public JmsConfig(String host, int port, String channel, String manager,
			String queues, int threads) {
		this.host = host;
		this.port = port;
		this.channel = channel;
		this.manager = manager;
		this.queues = queues;
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

	public String getQueues() {
		return queues;
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
		result = prime * result + ((queues == null) ? 0 : queues.hashCode());
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
		JmsConfig other = (JmsConfig) obj;
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
		if (queues == null) {
			if (other.queues != null)
				return false;
		} else if (!queues.equals(other.queues))
			return false;
		if (threads != other.threads)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JmsConfig [host=" + host + ", port=" + port + ", channel="
				+ channel + ", manager=" + manager + ", queues=" + queues
				+ ", threads=" + threads + "]";
	}

}
