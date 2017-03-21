package com.cheeray.jms;

import com.cheeray.jms.annotation.MAware;
import com.cheeray.jms.annotation.MConsumer;

@MAware
public class ConsumerRef {

	@MConsumer(queueRefs = { "inbound" })
	public void onInbound(String msg) throws InterruptedException {
		ResultsQueue.RESULTS.put(msg);
	}
}
