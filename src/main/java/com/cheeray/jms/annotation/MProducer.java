package com.cheeray.jms.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * Sending out the return value to the configured queue. If failed, send to
 * backout queues.
 * 
 * <p>
 * Annotate class with &#064;MAware to active.
 * 
 * <pre>
 *    Example1 : bind queues explicitly.
 * 
 *    &#064;MAware
 *    public class Sender{
 * 
 *    ...
 * 
 *    &#064;MProducer(value = {&#064;MQueue(channel = "C1", host = "H1", 
 *      	manager = "M1", port = 1234, queue = "Q1") },
 *    backouts = {&#064;MQueue(channel = "C2", host = "H2",
 *      	manager = "M2", port = 4321, queue = "BQ1") })
 *    protected String send() { return name; }
 * 
 *    ...
 *    }
 * 
 *    Example2 : runtime binding by reference.
 *    System properties:
 *    outbound.channel = "C1"
 *    outbound.host= = "H1"
 *    outbound.manager = "M1"
 *    outbound.port = 1234
 *    outbound.queue = "Q1"
 * 
 *    backout.channel = "C2"
 *    backout.host= = "H2"
 *    backout.manager = "M2"
 *    backout.port = 4321
 *    backout.queue = "BQ1"
 * 
 *    &#064;MProducer
 *    public class Sender{
 * 
 *    ...
 * 
 *    &#064;MProducer(queueRefs={"outbound"}, backoutRefs={"backout"})
 *    protected String send() { return name; }
 * 
 *    ...
 *    }
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
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MProducer {

	@Nonbinding
	public MQueue[] value() default {};

	@Nonbinding
	public MQueue[] backouts() default {};

	public BackoutMode backoutMode() default BackoutMode.EACH_TRIED_ALL_FAILED;

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
