package com.cheeray.se;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cheeray.jms.BackoutProducer;
import com.cheeray.jms.Producer;
import com.cheeray.jms.ProducerRef;
import com.cheeray.jms.ResultsQueue;
import com.cheeray.mq.Connectors;
import com.cheeray.test.WeldJUnit4Runner;

@RunWith(WeldJUnit4Runner.class)
public class IntegrationTest {

	@Inject
	private Producer producer;

	@Inject
	private BackoutProducer bkProdcuer;

	@Inject
	private ProducerRef producerRef;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("inbound.host", "MQHOST02");
		System.setProperty("inbound.port", "3434");
		System.setProperty("inbound.channel", "CLIENT.TO.MQ02");
		System.setProperty("inbound.manager", "MQ.MANAGER02");
		System.setProperty("inbound.queue", "MQ.QUEUE.IN");

		System.setProperty("outbound.host", "MQHOST02");
		System.setProperty("outbound.port", "3434");
		System.setProperty("outbound.channel", "CLIENT.TO.MQ02");
		System.setProperty("outbound.manager", "MQ.MANAGER02");
		System.setProperty("outbound.queue", "MQ.QUEUE.IN");

		System.setProperty("backout.host", "MQHOST01");
		System.setProperty("backout.port", "3434");
		System.setProperty("backout.channel", "CLIENT.TO.MQ01");
		System.setProperty("backout.manager", "MQ.MANAGER01");
		System.setProperty("backout.queue", "MQ.QUEUE.IN");
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@Before
	public void setUp() throws Exception {
		ResultsQueue.RESULTS.clear();
		try {
			Connectors.start();
		} catch (Exception e) {

		}
	}

	@After
	public void tearDown() throws Exception {
		try {
			Connectors.stop();
		} catch (Exception e) {

		}
		ResultsQueue.RESULTS.clear();
	}

	@Test
	public final void testProduceConsume() throws InterruptedException {
		final String exptected = producer.sendout("This is a test.");
		String actual = ResultsQueue.RESULTS.poll(1, TimeUnit.MINUTES);
		assertEquals(exptected, actual);
	}

	@Test
	public final void testProduceConsumeRef() throws InterruptedException {
		final String exptected = producerRef.sendout();
		String actual = ResultsQueue.RESULTS.poll(1, TimeUnit.MINUTES);
		assertEquals(exptected, actual);
	}

	@Test
	public final void testBackoutProduceConsume() throws InterruptedException {
		final String exptected = bkProdcuer.sendout();
		String actual = ResultsQueue.RESULTS.poll(1, TimeUnit.MINUTES);
		assertEquals(exptected, actual);
	}
}
