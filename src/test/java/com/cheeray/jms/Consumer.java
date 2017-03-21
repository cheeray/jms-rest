package com.cheeray.jms;

import com.cheeray.jms.annotation.MAware;
import com.cheeray.jms.annotation.MConsumer;
import com.cheeray.jms.annotation.MQueue;

@MAware
public class Consumer {

	@MConsumer({ @MQueue(channel = "CLIENT.TO.MQ01", host = "MQHOST01",
			manager = "MQ.MANAGER01", port = 3434, queue = "MQ.QUEUE.IN") })
	public void onInbound(String msg) throws InterruptedException {
		ResultsQueue.RESULTS.put(msg);
	}
}
