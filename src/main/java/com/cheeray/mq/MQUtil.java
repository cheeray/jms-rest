package com.cheeray.mq;

import static com.ibm.mq.constants.CMQC.CHANNEL_PROPERTY;
import static com.ibm.mq.constants.CMQC.HOST_NAME_PROPERTY;
import static com.ibm.mq.constants.CMQC.PORT_PROPERTY;
import static com.ibm.mq.constants.CMQC.TRANSPORT_MQSERIES_CLIENT;
import static com.ibm.mq.constants.CMQC.TRANSPORT_PROPERTY;

import java.util.Properties;

import com.cheeray.jms.QueueConfig;

public class MQUtil {

	public static Properties toProperties(QueueConfig cfg) {
		final Properties props = new Properties();
		props.put(HOST_NAME_PROPERTY, cfg.getHost()); // required
		props.put(PORT_PROPERTY, cfg.getPort()); // required
		props.put(CHANNEL_PROPERTY, cfg.getChannel()); // required
		props.put(TRANSPORT_PROPERTY, TRANSPORT_MQSERIES_CLIENT); // opt
		return props;
	}
}
