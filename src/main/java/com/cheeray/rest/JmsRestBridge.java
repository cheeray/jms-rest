package com.cheeray.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cheeray.exception.IntegrationException;
import com.cheeray.rest.config.BridgeConfig;
import com.cheeray.rest.config.Config;
import com.cheeray.rest.config.JmsConfig;
import com.cheeray.rest.config.RestConfig;
import com.cheeray.rest.config.SslConfig;
import com.ibm.mq.MQException;

/**
 * JSE standalone bootstrap.
 * 
 * @author Chengwei.Yan
 * 
 */
public abstract class JmsRestBridge {
	private static final Logger LOG = LoggerFactory
			.getLogger(JmsRestBridge.class);

	public static void main(String[] args) throws FileNotFoundException,
			IOException, IntegrationException, MQException {
		if (args.length < 1) {
			System.err.println("Please provide a YAML configration file.");
			System.exit(1);
		}

		final File f = new File(args[0]);
		if (!f.exists()) {
			System.err.println("YAML file " + args[0] + " not found.");
			System.exit(1);
		}
		if (f.isDirectory()) {
			System.err.println("YAML file " + args[0]
					+ " cannot be a directory.");
			System.exit(1);
		}

		BasicConfigurator.configure();
		final BridgeConfig config = Config.parse(f);

		final JmsConfig inbound = config.getInbound();
		final RestConfig remote = config.getRemote();
		if (inbound == null ^ remote == null) {
			System.err.println("Inbound and remote must be paired up.");
			System.exit(1);
		}

		final RestConfig local = config.getLocal();
		final JmsConfig outbound = config.getOutbound();
		final SslConfig sslConfig = config.getSslConfig();
		if (local == null ^ outbound == null) {
			System.err.println("Outbound and local must be paired up.");
			System.exit(1);
		}

		if (inbound != null) {
			LOG.info("Bridge JMS {} to REST {}.", inbound, remote);
			try {
				final JmsToRest jmsRest = new JmsToRest(inbound, remote);
				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

					public void run() {
						jmsRest.stop();
					}
				}));
				jmsRest.start();
			} catch (Exception e) {
				LOG.error("Failed bridge JMS to REST.", e);
				System.err.println("Bridge JMS to REST failed.");
				System.exit(1);
			}

		}

		if (outbound != null) {
			LOG.info("Bridge REST {} to JMS {}.", local, outbound);
			try {
				final RestToJms restJms = new RestToJms(local, outbound, sslConfig);
				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

					public void run() {
						restJms.stop();
					}
				}));
				restJms.start();
			} catch (Exception e) {
				LOG.error("Failed bridge REST to JMS.", e);
				System.err.println("Bridge REST to JMS failed.");
				System.exit(1);
			}
		}
	}

}
