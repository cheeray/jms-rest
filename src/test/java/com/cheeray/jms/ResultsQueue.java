package com.cheeray.jms;

import java.util.concurrent.ArrayBlockingQueue;

public class ResultsQueue {

	public static final ArrayBlockingQueue<String> RESULTS = new ArrayBlockingQueue<>(
			10);
}
