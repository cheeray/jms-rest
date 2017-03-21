package com.cheeray.jms;

import com.cheeray.jms.annotation.MAware;
import com.cheeray.jms.annotation.MProducer;
import com.cheeray.jms.annotation.MQueue;

@MAware
public class Producer {

	@MProducer(value = { @MQueue(channel = "CLIENT.TO.MQ01",
			host = "MQHOST01", manager = "MQ.MANAGER01", port = 3434,
			queue = "MQ.QUEUE.IN") }, backouts = { @MQueue(
			channel = "CLIENT.TO.MQ02", host = "MQHOST02",
			manager = "MQ.MANAGER02", port = 3434, queue = "MQ.QUEUE.IN") })
	public String sendout(String s) {
		return s;
	}
}
