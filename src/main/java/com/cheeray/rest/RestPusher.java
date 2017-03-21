package com.cheeray.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cheeray.rest.config.RestConfig;

/**
 * Push JMS to receiver.
 * 
 * @author Chengwei.Yan
 * 
 */
public class RestPusher {
	private static final Logger LOG = LoggerFactory.getLogger(RestPusher.class);
	private final URL url;
	private final String credentials;
	private final String method;
	
	public RestPusher(RestConfig rest) throws MalformedURLException {

		this.url = new URL(rest.getScheme(), rest.getServer(), rest.getPort(),
				rest.getContext());
		if (rest.getUserName() != null && rest.getPassword() != null) {
			this.credentials = rest.getUserName() + ":" + rest.getPassword();
		} else {
			this.credentials = null;
		}
		this.method = rest.getMethod();
	}

	public void push(byte[] data, String msgId) {
		try {
			final HttpURLConnection conn = (HttpURLConnection) url
					.openConnection();
			if (credentials != null) {
				String basicAuth = "Basic "
						+ new String(
								Base64.encodeBase64(credentials.getBytes()));
				conn.setRequestProperty("Authorization", basicAuth);
			}
			conn.setRequestMethod(method);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length",
					"" + Integer.toString(data.length));
			conn.setRequestProperty("Content-Language", "en-US");
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);

			try (final OutputStream os = conn.getOutputStream()) {
				os.write(data);
			}
			switch (conn.getResponseCode()) {
			case 200:
				LOG.info("Message {} pushed successfully: {}.", msgId,
						conn.getResponseMessage());
				break;
			default:
				LOG.error("Message {} pushed failed with {} response: {}.",
						msgId, conn.getResponseCode(),
						conn.getResponseMessage());
				break;
			}
			conn.disconnect();
		} catch (IOException e) {
			LOG.error("Failed push message " + msgId + " to " + url, e);
		}
	}
}
