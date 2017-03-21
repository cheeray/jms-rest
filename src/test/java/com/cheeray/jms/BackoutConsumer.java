package com.cheeray.jms;

import com.cheeray.jms.annotation.MAware;
import com.cheeray.jms.annotation.MConsumer;
import com.cheeray.jms.annotation.MQueue;

@MAware
public class BackoutConsumer {

	@MConsumer({ @MQueue(channel = "CLIENT.TO.MQ02", host = "MQHOST02",
			manager = "MQ.MANAGER02", port = 3434, queue = "MQ.QUEUE.IN") })
	public void onInbound(String msg) throws InterruptedException {
		ResultsQueue.RESULTS.put(msg);
	}
}
