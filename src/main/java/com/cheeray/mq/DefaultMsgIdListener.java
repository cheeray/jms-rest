package com.cheeray.mq;

public class DefaultMsgIdListener implements MsgIdListener {
	private String queue;
	private String msgId;

	@Override
	public void on(String queue, String msgId) {
		this.queue = queue;
		this.msgId = msgId;
	}

	public String getQueue() {
		return queue;
	}

	public String getMsgId() {
		return msgId;
	}

}
