package com.cheeray.jms.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Define a queue.
 * 
 * @author Chengwei.Yan
 * 
 */
@Target({})
@Retention(RUNTIME)
public @interface MQueue {
	/**
	 * (Required) The queue host name.
	 */
	public String host();

	/**
	 * (Required) The port of queue.
	 */
	public int port();

	/**
	 * (Required) The channel.
	 */
	public String channel();

	/**
	 * (Required) The queue manager name.
	 */
	public String manager();

	/**
	 * (Required) The queue name.
	 */
	public String queue();

	/**
	 * (Optional) Max threads.
	 */
	public int threads() default 1;

}
