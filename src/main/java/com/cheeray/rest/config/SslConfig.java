package com.cheeray.rest.config;

public class SslConfig {

	private final String keystore;
	private final String truststore;
	private final String password;

	public SslConfig(final String keystore, final String truststore,
			final String password) {
		this.keystore = keystore;
		this.truststore = truststore;
		this.password = password;
	}

	public String getKeystore() {
		return keystore;
	}

	public String getTruststore() {
		return truststore;
	}

	public String getPassword() {
		return password;
	}

}
