package com.cheeray.rest;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowLogger;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.util.HttpString;
import io.undertow.util.MalformedMessageException;
import io.undertow.util.StatusCodes;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.ChannelListener;
import org.xnio.Pooled;
import org.xnio.channels.StreamSourceChannel;

import com.cheeray.jms.QueueConfig;
import com.cheeray.jms.annotation.BackoutMode;
import com.cheeray.mq.MQProducer;
import com.cheeray.rest.config.JmsConfig;
import com.cheeray.rest.config.RestConfig;
import com.cheeray.rest.config.SslConfig;

/**
 * Publish rest data to JMS queue.
 * 
 * @author Chengwei.Yan
 * 
 */
public class RestToJms {
	private static final Logger LOG = LoggerFactory.getLogger(RestToJms.class);
	private final Undertow server;

	public RestToJms(RestConfig rest, JmsConfig jms, SslConfig sslConfig)
			throws Exception {
		final String[] queues = jms.getQueues().split(",");
		if (queues.length <= 0) {
			throw new IllegalArgumentException("No queues to consume.");
		}
		final List<QueueConfig> cfgs = new ArrayList<>(queues.length);
		for (String q : queues) {
			cfgs.add(new QueueConfig(jms.getHost(), jms.getPort(), jms
					.getChannel(), jms.getManager(), q, 1));
		}

		final RoutingHandler routingHandler = Handlers.routing();
		routingHandler.add(new HttpString(rest.getMethod()), rest.getContext(),
				new PutHandler(cfgs));
		if (rest.getScheme().equalsIgnoreCase("https")) {
			if (sslConfig != null) {
				if (sslConfig.getPassword() == null) {
					throw new IllegalArgumentException(
							"Missing password in SSL configuration.");
				}
				final char[] pwd = sslConfig.getPassword().toCharArray();
				SSLContext sslContext = createSSLContext(
						loadKeyStore(sslConfig.getKeystore(), pwd),
						loadKeyStore(sslConfig.getTruststore(), pwd), pwd);
				this.server = Undertow
						.builder()
						.addHttpsListener(rest.getPort(), rest.getServer(),
								sslContext).setHandler(routingHandler).build();
			} else {
				throw new IllegalArgumentException("Missing SSL configuration.");
			}
		} else {
			this.server = Undertow.builder()
					.addHttpListener(rest.getPort(), rest.getServer())
					.setHandler(routingHandler).build();
		}
	}

	private static KeyStore loadKeyStore(String name, char[] password)
			throws Exception {
		String storeLoc = System.getProperty(name);
		final InputStream stream;
		if (storeLoc == null) {
			stream = RestToJms.class.getResourceAsStream(name);
		} else {
			stream = Files.newInputStream(Paths.get(storeLoc));
		}

		try (InputStream is = stream) {
			KeyStore loadedKeystore = KeyStore.getInstance("JKS");
			loadedKeystore.load(is, password);
			return loadedKeystore;
		}
	}

	private static SSLContext createSSLContext(final KeyStore keyStore,
			final KeyStore trustStore, final char[] password) throws Exception {
		KeyManager[] keyManagers;
		KeyManagerFactory keyManagerFactory = KeyManagerFactory
				.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, password);
		keyManagers = keyManagerFactory.getKeyManagers();

		TrustManager[] trustManagers;
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(trustStore);
		trustManagers = trustManagerFactory.getTrustManagers();

		SSLContext sslContext;
		sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagers, trustManagers, null);

		return sslContext;
	}

	public void stop() {
		LOG.info("Stop local Restful server.");
		server.stop();
	}

	public void start() {
		server.start();
	}

	private static final class PutHandler implements HttpHandler {
		private List<QueueConfig> cfgs;

		public PutHandler(List<QueueConfig> cfgs) {
			this.cfgs = cfgs;
		}

		@Override
		public void handleRequest(final HttpServerExchange exchange)
				throws Exception {
			if (exchange.isInIoThread()) {
				exchange.dispatch(new DispatchTask(cfgs, exchange));
			}
		}
	}

	private static final class DispatchTask implements Runnable {
		private final List<QueueConfig> cfgs;
		private final HttpServerExchange exchange;
		private final Executor executor;
		private final StreamSourceChannel requestChannel;

		public DispatchTask(List<QueueConfig> cfgs, HttpServerExchange exchange) {
			this.cfgs = cfgs;
			this.exchange = exchange;
			this.executor = exchange.getConnection().getWorker();
			this.requestChannel = exchange.getRequestChannel();
		}

		/*
		 * @Override public void handleRequest(final HttpServerExchange
		 * exchange) throws Exception { ChannelListener<?> listener; //
		 * exchange.getRequestChannel().getReadSetter().set(listener); //
		 * Pooled<ByteBuffer> p =
		 * exchange.getConnection().getBufferPool().allocate(); // ByteBuffer bb
		 * = p.getResource(); // exchange.startBlocking(); // final String
		 * content = new String(bb.array(), "UTF-8"); // LOG.debug(content);
		 * Builder builder = FormParserFactory.builder();
		 * builder.setDefaultCharset("UTF-8"); final FormDataParser
		 * formDataParser = builder.build().createParser( exchange); if
		 * (formDataParser != null) { exchange.startBlocking(); FormData
		 * formData = formDataParser.parseBlocking();
		 * 
		 * for (String data : formData) { if (data.equalsIgnoreCase("msg")) {
		 * for (FormData.FormValue formValue : formData.get(data)) { // Send
		 * them out ... MQProducer.send(cfgs, new ArrayList<QueueConfig>(),
		 * BackoutMode.EACH_TRIED_ANY_FAILURE,
		 * formValue.getValue().getBytes("UTF-8")); } } } } else {
		 * LOG.info("No msg in form data, ignored.");
		 * exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,
		 * "text/plain");
		 * exchange.getResponseSender().send("Missing msg in form data."); }
		 * exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,
		 * "text/plain"); exchange.getResponseSender().send("OK"); }
		 */
		@Override
		public void run() {
			try {
				Pooled<ByteBuffer> pooled = exchange.getConnection()
						.getBufferPool().allocate();
				try {
					while (true) {
						ByteBuffer buff = pooled.getResource();
						buff.clear();
						int c = requestChannel.read(buff);
						if (c == 0) {
							requestChannel.getReadSetter().set(
									new ChannelListener<StreamSourceChannel>() {
										@Override
										public void handleEvent(
												StreamSourceChannel channel) {
											channel.suspendReads();
											executor.execute(DispatchTask.this);
										}
									});
							requestChannel.resumeReads();
							return;
						} else if (c == -1) {
							System.out.print(buff);
							return;
						} else {
							int size = buff.remaining();
							buff.flip();
							size = buff.remaining();
							byte[] bytes = new byte[size];
							int i = 0;
							while (buff.hasRemaining()) {
								bytes[i++] = buff.get();
							}
							buff.compact();

							String content = new String(bytes, "UTF-8");
							LOG.debug(content);
							// Send them out ...
							MQProducer.send(cfgs, new ArrayList<QueueConfig>(),
									BackoutMode.EACH_TRIED_ANY_FAILURE, bytes);

							exchange.setResponseCode(StatusCodes.OK);
							exchange.endExchange();
						}
					}
				} catch (MalformedMessageException e) {
					UndertowLogger.REQUEST_IO_LOGGER.ioException(e);
					exchange.setResponseCode(StatusCodes.INTERNAL_SERVER_ERROR);
					exchange.endExchange();
				} finally {
					pooled.free();
				}

			} catch (Throwable e) {
				UndertowLogger.REQUEST_IO_LOGGER.debug(
						"Exception parsing data", e);
				exchange.setResponseCode(StatusCodes.INTERNAL_SERVER_ERROR);
				exchange.endExchange();
			}
		}
	}
}
