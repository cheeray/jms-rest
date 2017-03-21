package com.cheeray.cdi;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cheeray.jms.QueueMethodHandle;
import com.cheeray.jms.Transporters;
import com.cheeray.jms.annotation.MProducer;
import com.cheeray.mq.MQProducer;
import com.cheeray.mq.MsgIdListener;

@MProducer
@Priority(Interceptor.Priority.APPLICATION+10)
@Interceptor
public class ProduceInterceptor implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ProduceInterceptor.class);

	@AroundInvoke
	public Object arroundInvoking(InvocationContext ic) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Around " + ic.getTarget().getClass() + "." + ic.getMethod().getName());
		}
		final Object result = ic.proceed();
		if (result != null) {
			final Method method = ic.getMethod();
			if (method.isAnnotationPresent(MProducer.class)) {
				final Class<?> cls = method.getDeclaringClass();
				final QueueMethodHandle qmh = Transporters.PRODUCERS.get(cls, method.getName());
				// TODO: Use converter to convert result into byte[].
				try {
					MsgIdListener listener = null;
					for (Object o : ic.getParameters()) {
						if (MsgIdListener.class.isInstance(o)) {
							listener = MsgIdListener.class.cast(o);
						}
					}
					// TODO: type safety and consumer's parameters type.
					MQProducer.send(qmh.getQueues(), qmh.getBackouts(), qmh.getBackoutMode(),
							result.toString().getBytes("UTF-8"), listener);
				} catch (Exception e) {
					// TODO: Send to alternative queue ...
					throw e;
				}
			} else {
				LOG.warn("Ignored method " + method.getName() + " because missing annotation @Produce.");
			}
		} else if (LOG.isDebugEnabled()) {
			LOG.debug("No result to send for " + ic.getTarget().getClass() + "." + ic.getMethod().getName());
		}
		return result;
	}
}
