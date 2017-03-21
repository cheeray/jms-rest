package com.cheeray.test;

import org.apache.log4j.BasicConfigurator;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import com.cheeray.se.WeldContext;

public class WeldJUnit4Runner extends BlockJUnit4ClassRunner {

	public WeldJUnit4Runner(Class<Object> clazz) throws InitializationError {
		super(clazz);
		BasicConfigurator.configure();
	}

	@Override
	protected Object createTest() {
		final Class<?> test = getTestClass().getJavaClass();
		return WeldContext.getInstance().getBean(test);
	}
}