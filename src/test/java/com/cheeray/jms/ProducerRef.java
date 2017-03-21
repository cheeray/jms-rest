package com.cheeray.jms;

import com.cheeray.jms.annotation.MAware;
import com.cheeray.jms.annotation.MProducer;

@MAware
public class ProducerRef {

	@MProducer(queueRefs = { "outbound" }, backoutRefs = { "backout" })
	public String sendout() {
		return "This is a dynamic configuration test.";
	}
}
