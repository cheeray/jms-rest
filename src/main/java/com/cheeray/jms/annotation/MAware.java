package com.cheeray.jms.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Message aware class which produce or consume messages from one or more of its
 * annotated methods.
 * 
 * <p>
 * Annotate class with &#064;MAware to active its queue ability.
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
 * 
 *    &#064;MConsumer(queueRefs={"inbound"}, backoutRefs={"backout"})
 *    protected void onMsg(String msg) {
 *      ... 
 *    }
 *    ...
 *    }
 * 
 * </pre>
 * 
 * @see MProducer
 * @see MConsumer
 * @author Chengwei.Yan
 * 
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MAware {
}
