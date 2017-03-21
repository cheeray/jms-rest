package com.cheeray.jms.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * Listen on JMS messages to trigger the processing.
 * 
 * <p>
 * Annotate class with &#064;MAware to active.
 * 
 * <pre>
 *    Example1 : bind queues explicitly.
 * 
 *    &#064;MAware
 *    public class Listener{
 * 
 *    ...
 * 
 *    &#064;MConsumer(value={&#064;MQueue(channel = "C1", host = "H1", 
 *      	manager = "M1", port = 1234, queue = "Q1"),
 *      backouts = {&#064;MQueue(channel = "C2", host = "H2",
 *      	manager = "M2", port = 4321, queue = "BQ1") })
 *    protected void onMsg(String msg) {
 *      ... 
 *    }
 * 
 *    ...
 *    }
 *    
 *    
 *    Example2 : runtime binding by reference.
 * 
 *    System properties:
 *    inbound.channel = "C1"
 *    inbound.host= = "H1"
 *    inbound.manager = "M1"
 *    inbound.port = 1234
 *    inbound.queue = "Q1"
 * 
 *    backout.channel = "C2"
 *    backout.host= = "H2"
 *    backout.manager = "M2"
 *    backout.port = 4321
 *    backout.queue = "BQ1"
 * 
 *    &#064;MConsumer
 *    public class Listener{
 * 
 *    ...
 * 
 *    &#064;MConsumer(queueRefs={"inbound"}, backoutRefs={"backout"})
 *    protected void onMsg(String msg) {
 *      ... 
 *    }
 * 
 *    ...
 *    }
 * 
 * 
 * </pre>
 * 
 * @see MAware
 * @see MQueue
 * @author Chengwei.Yan
 * 
 */
@Inherited
@InterceptorBinding
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MConsumer {
	@Nonbinding
	public MQueue[] value() default {};

	@Nonbinding
	public MQueue[] backouts() default {};

	/**
	 * Queue reference to system property. Ignored if value is given.
	 * 
	 * @return prefix of queue properties.
	 */
	@Nonbinding
	public String[] queueRefs() default {};

	/**
	 * Backout queue reference to system property. Ignored if value is given.
	 * 
	 * @return prefix of backout queue properties.
	 */
	@Nonbinding
	public String[] backoutRefs() default {};

}
