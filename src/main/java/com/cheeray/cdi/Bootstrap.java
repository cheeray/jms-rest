package com.cheeray.cdi;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cheeray.jms.QueueConfig;
import com.cheeray.jms.QueueMethodHandle;
import com.cheeray.jms.Transporters;
import com.cheeray.jms.annotation.BackoutMode;
import com.cheeray.jms.annotation.MAware;
import com.cheeray.jms.annotation.MConsumer;
import com.cheeray.jms.annotation.MProducer;
import com.cheeray.jms.annotation.MQueue;
import com.cheeray.mq.Connectors;

/**
 * Service provider observers any container lifecycle events and obtain an
 * injected javax.enterprise.inject.spi.BeanManager.
 * 
 * 
 * 
 * @author Chengwei.Yan
 * 
 */
public class Bootstrap implements Extension {
	private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);
	private static final List<CreationalContext<?>> CCTX = new ArrayList<>();

	/**
	 * Load consumers and producers.
	 */
	<A, T> void processAnnotatedType(
			@Observes @WithAnnotations({ MAware.class }) ProcessAnnotatedType<A> pat) {
		final AnnotatedType<A> at = pat.getAnnotatedType();
		// Consumers ...
		final Class<A> c = at.getJavaClass();

		final Lookup lookup = MethodHandles.lookup();
		for (AnnotatedMethod<?> am : at.getMethods()) {
			// Parse producers ...
			if (am.isAnnotationPresent(MProducer.class)) {
				final MProducer mp = am.getAnnotation(MProducer.class);
				if (mp.value().length > 0) {
					final List<QueueConfig> qs = new ArrayList<>();
					for (MQueue q : mp.value()) {
						qs.add(new QueueConfig(q));
					}
					final List<QueueConfig> bkqs = new ArrayList<>();
					for (MQueue q : mp.backouts()) {
						bkqs.add(new QueueConfig(q));
					}
					addProducer(c, lookup, am, qs, bkqs, mp.backoutMode());
				} else {
					final String[] queueRefs = mp.queueRefs();
					if (queueRefs.length > 0) {
						addProducer(c, lookup, am,
								parseQueueConfigs(queueRefs),
								parseQueueConfigs(mp.backoutRefs()),
								mp.backoutMode());
					} else {
						throw new InjectionException(c + "." + am
								+ " is missing @MQueue value or queueRef.");
					}
				}
			}

			// Parse consumers ...
			if (am.isAnnotationPresent(MConsumer.class)) {
				final MConsumer mc = am.getAnnotation(MConsumer.class);
				if (mc.value().length > 0) {
					final List<QueueConfig> qs = new ArrayList<>();
					for (MQueue q : mc.value()) {
						qs.add(new QueueConfig(q));
					}
					final List<QueueConfig> bkqs = new ArrayList<>();
					for (MQueue q : mc.backouts()) {
						bkqs.add(new QueueConfig(q));
					}
					addConsumers(c, lookup, am, qs, bkqs);
				} else {
					final String[] queueRefs = mc.queueRefs();

					if (queueRefs.length > 0) {
						addConsumers(c, lookup, am,
								parseQueueConfigs(queueRefs),
								parseQueueConfigs(mc.backoutRefs()));
					} else {
						throw new InjectionException(c + "." + am
								+ " is missing @MQueue value or queueRef.");
					}
				}
			}

		}
	}

	private static List<QueueConfig> parseQueueConfigs(final String[] queueRefs) {
		final List<QueueConfig> cfgs = new ArrayList<>();
		for (String r : queueRefs) {
			cfgs.add(new QueueConfig(System.getProperty(r + ".host"), Integer
					.parseInt(System.getProperty(r + ".port", "-1")), System
					.getProperty(r + ".channel"), System.getProperty(r
					+ ".manager"), System.getProperty(r + ".queue"), Integer
					.parseInt(System.getProperty(r + ".threads", "1"))));
		}
		return cfgs;
	}

	private static <A> void addProducer(final Class<A> c, final Lookup lookup,
			AnnotatedMethod<?> am, final List<QueueConfig> qs,
			final List<QueueConfig> bkqs, final BackoutMode backoutMode) {
		try {
			final Method m = am.getJavaMember();
			MethodHandle mh = lookup.unreflect(m);
			final QueueMethodHandle qmh = new QueueMethodHandle(c, mh, qs,
					bkqs, backoutMode);
			LOG.info("Will intercept JMS producer {}.{}.", c,
					am.getJavaMember());
			Transporters.PRODUCERS.add(c, am.getJavaMember().getName(), qmh);
		} catch (IllegalAccessException e) {
			throw new InjectionException(c + "." + am + " is not accessible.");
		}
	}

	private static <A> void addConsumers(final Class<A> c, final Lookup lookup,
			AnnotatedMethod<?> am, final List<QueueConfig> qs,
			final List<QueueConfig> bkqs) {
		try {
			final Method m = am.getJavaMember();
			final MethodHandle mh = lookup.unreflect(m);
			final QueueMethodHandle qmh = new QueueMethodHandle(c, mh, qs, bkqs);
			LOG.info("Will intercept JMS consumer {}.{}.", c,
					am.getJavaMember());
			Transporters.CONSUMERS.add(c, am.getJavaMember().getName(), qmh);
		} catch (IllegalAccessException e) {
			throw new InjectionException(c + "." + am + " is not accessible.");
		}
	}

	void afterDeploymentValidation(@Observes AfterDeploymentValidation adv,
			BeanManager bm) {
		LOG.info("Init all JMS connectors.");
		for (QueueMethodHandle qmh : Transporters.CONSUMERS.handles()) {
			Object consumer = null;
			final Bean<?> b = getConsumerBean(bm.getBeans(qmh.getType()),
					qmh.getQueues());
			if (b != null) {
				final CreationalContext<?> ctx = bm.createCreationalContext(b);
				CCTX.add(ctx);
				consumer = bm.getReference(b, b.getBeanClass(), ctx);
			}

			if (consumer != null) {
				qmh.setInstance(consumer);
			} else {
				// TODO: handle POJO consumers ...
				LOG.error("Not found consumer {} to wire up.", qmh.getType());
				adv.addDeploymentProblem(new RuntimeException("Consumer "
						+ qmh.getType() + " is not injectable."));
			}
		}

		try {
			Connectors.init();
		} catch (Exception e) {
			adv.addDeploymentProblem(e);
		}
	}

	public void onShutdown(@Observes BeforeShutdown bse) {
		LOG.info("Release all contexts.");
		for (CreationalContext<?> ctx : CCTX) {
			ctx.release();
		}
	}

	public static Bean<?> getConsumerBean(Set<Bean<?>> beans,
			List<QueueConfig> queues) {
		for (Bean<?> b : beans) {
			Class<?> bc = b.getBeanClass();
			for (Method m : bc.getDeclaredMethods()) {
				if (m.isAnnotationPresent(MConsumer.class)) {
					final MConsumer mc = m.getAnnotation(MConsumer.class);
					final List<QueueConfig> cfgs = new ArrayList<>(queues);
					for (MQueue q : mc.value()) {
						cfgs.remove(new QueueConfig(q));
					}
					cfgs.removeAll(parseQueueConfigs(mc.queueRefs()));
					if (cfgs.isEmpty()) {
						return b;
					}
				}
			}
		}
		return null;
	}
}
