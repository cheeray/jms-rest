package com.cheeray.mq;

/**
 * Listen on message ID.
 * 
 * @author Chengwei.Yan
 * 
 */
public interface MsgIdListener {

	/**
	 * Handle message ID.
	 * 
	 * @param queue
	 *            The queue name.
	 * @param msgId
	 *            The message ID.
	 */
	public void on(String queue, String msgId);
}
