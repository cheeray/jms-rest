package com.cheeray.rest.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimpleYaml {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleYaml.class);

	public static <T> T parse(File f, Class<T> c) throws IOException {
		final List<String> lines = Files.readAllLines(Paths.get(f.getPath()),
				Charset.forName("UTF-8"));
		return parse(c, lines, new AtomicInteger(0));
	}

	public static <T> T parse(Class<T> c, List<String> lines,
			AtomicInteger index) throws IOException {
		final Map<String, Field> fs = new LinkedHashMap<>();
		final Map<String, Object> fv = new HashMap<>();
		for (Field f : c.getDeclaredFields()) {
			fs.put(f.getName(), f);
		}

		while (index.get() < lines.size()) {
			String line = lines.get(index.get());
			if (!line.trim().isEmpty() && !line.trim().startsWith("#")
					&& line.trim().contains(":")) {
				final String[] kv = line.trim().split(":");
				final String k = kv[0];
				final Field f = fs.get(k);
				if (f != null) {
					index.incrementAndGet();
					if (kv.length == 2) {
						fv.put(k, kv[1]);
					} else {
						// Sub class
						fv.put(k, parse(f.getType(), lines, index));
					}
				} else {
					break;
				}
			} else {
				index.incrementAndGet();
			}
		}
		for (Constructor<?> cs : c.getConstructors()) {
			try {
				final Class<?>[] paramTypes = cs.getParameterTypes();
				final Object[] params = new Object[paramTypes.length];
				int i = 0;
				for (Map.Entry<String, Field> fe : fs.entrySet()) {
					params[i] = convert(paramTypes[i], fv.get(fe.getKey()));
					i++;
				}
				@SuppressWarnings("unchecked")
				T obj = (T) cs.newInstance(params);
				return obj;
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				LOG.error("Failed construct " + c
						+ ". Please check the order of parameters.", e);
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private static <T> T convert(Class<T> c, Object object) {
		if (object == null)
			return null;
		if (c.isAssignableFrom(object.getClass())) {
			return (T) object;
		}
		if (String.class.isAssignableFrom(c)) {
			return (T) object.toString();
		} else if (c.isPrimitive()) {
			switch (c.getName()) {
			case "int":
				return (T) Integer.valueOf(object.toString());
			default:

			}
		}
		return null;
	}

}
