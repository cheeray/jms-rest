package com.cheeray.rest.config;

public class RestConfig {

	private final String scheme;
	private final String server;
	private final int port;
	private final String context;
	private final String userName;
	private final String password;
	private final String method;

	public RestConfig(final String scheme, final String server, final int port, final String context,
			final String userName, final String password, final String method) {
		this.scheme = scheme;
		this.server = server;
		this.port = port;
		this.context = context;
		this.userName = userName;
		this.password = password;
		this.method = method;
	}

	public String getScheme() {
		return scheme;
	}

	public String getServer() {
		return server;
	}

	public int getPort() {
		return port;
	}

	public String getContext() {
		return context;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getMethod() {
		return method;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result + port;
		result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		RestConfig other = (RestConfig) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		if (port != other.port)
			return false;
		if (scheme == null) {
			if (other.scheme != null)
				return false;
		} else if (!scheme.equals(other.scheme))
			return false;
		if (server == null) {
			if (other.server != null)
				return false;
		} else if (!server.equals(other.server))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RestConfig [scheme=" + scheme + ", server=" + server + ", port=" + port + ", context=" + context
				+ ", method=" + method + "]";
	}

}
