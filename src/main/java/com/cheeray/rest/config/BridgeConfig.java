package com.cheeray.rest.config;

public class BridgeConfig {

	private final JmsConfig inbound;
	private final JmsConfig outbound;

	private final RestConfig local;
	private final RestConfig remote;

	private final SslConfig sslConfig;

	public BridgeConfig(JmsConfig inbound, JmsConfig outbound,
			RestConfig local, RestConfig remote, SslConfig sslConfig) {
		this.inbound = inbound;
		this.outbound = outbound;
		this.local = local;
		this.remote = remote;
		this.sslConfig = sslConfig;
	}

	public JmsConfig getInbound() {
		return inbound;
	}

	public JmsConfig getOutbound() {
		return outbound;
	}

	public RestConfig getLocal() {
		return local;
	}

	public RestConfig getRemote() {
		return remote;
	}

	public SslConfig getSslConfig() {
		return sslConfig;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inbound == null) ? 0 : inbound.hashCode());
		result = prime * result + ((local == null) ? 0 : local.hashCode());
		result = prime * result
				+ ((outbound == null) ? 0 : outbound.hashCode());
		result = prime * result + ((remote == null) ? 0 : remote.hashCode());
		result = prime * result
				+ ((sslConfig == null) ? 0 : sslConfig.hashCode());
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
		BridgeConfig other = (BridgeConfig) obj;
		if (inbound == null) {
			if (other.inbound != null)
				return false;
		} else if (!inbound.equals(other.inbound))
			return false;
		if (local == null) {
			if (other.local != null)
				return false;
		} else if (!local.equals(other.local))
			return false;
		if (outbound == null) {
			if (other.outbound != null)
				return false;
		} else if (!outbound.equals(other.outbound))
			return false;
		if (remote == null) {
			if (other.remote != null)
				return false;
		} else if (!remote.equals(other.remote))
			return false;
		if (sslConfig == null) {
			if (other.sslConfig != null)
				return false;
		} else if (!sslConfig.equals(other.sslConfig))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BridgeConfig [inbound=" + inbound + ", outbound=" + outbound
				+ ", local=" + local + ", remote=" + remote + ", ssl="
				+ (sslConfig != null) + "]";
	}

}
