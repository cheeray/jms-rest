package com.cheeray.jms;

import com.cheeray.jms.annotation.MAware;
import com.cheeray.jms.annotation.MProducer;
import com.cheeray.jms.annotation.MQueue;

@MAware
public class BackoutProducer {

	@MProducer(value = { @MQueue(channel = "CLIENT.TO.MQ03",
			host = "MQHOST01", manager = "MQ.MANAGER01", port = 3434,
			queue = "MQ.QUEUE.IN") }, backouts = { @MQueue(
			channel = "CLIENT.TO.MQ02", host = "MQHOST02",
			manager = "MQ.MANAGER02", port = 3434, queue = "MQ.QUEUE.IN") })
	public String sendout() {
		return "This is a test.";
	}
}
