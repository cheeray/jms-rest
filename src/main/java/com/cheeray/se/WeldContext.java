package com.cheeray.se;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import com.cheeray.cdi.Bootstrap;

public class WeldContext {

	private final Weld weld;
	private final WeldContainer container;

	private static class WeldHolder {
		private static final WeldContext INSTANCE = new WeldContext();
	}

	public static WeldContext getInstance() {
		return WeldHolder.INSTANCE;
	}

	private WeldContext() {
		this.weld = new Weld();
		Bootstrap boot = new Bootstrap();
		weld.addExtension(boot);
		this.container = weld.initialize();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				weld.shutdown();
			}
		});
	}

	public <T> T getBean(Class<T> type) {
		return container.instance().select(type).get();
	}
}
