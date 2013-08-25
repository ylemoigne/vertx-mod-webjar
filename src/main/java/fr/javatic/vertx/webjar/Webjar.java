/*
 * ******************************************************************************
 * Copyright (c) 2013 JavaTIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Yann Le Moigne - initial API and implementation
 * *****************************************************************************
 */

package fr.javatic.vertx.webjar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.vertx.java.core.buffer.Buffer;

import io.netty.buffer.ByteBufInputStream;

public class Webjar {
	private final static String WEBJAR_CONTENT_PREFIX = "META-INF/resources/";

	private Map<String, String> mapPathContent;

	public Webjar(Buffer buffer) {
		mapPathContent = new HashMap<>();

		try (JarInputStream jar = new JarInputStream(new ByteBufInputStream(buffer.getByteBuf()))) {
			JarEntry jarEntry;
			while ((jarEntry = jar.getNextJarEntry()) != null) {
				if (!isWebjarResource(jarEntry)) {
					continue;
				}

				String path = jarEntryAsPath(jarEntry);
				String content = getCurrentEntryContent(jar);

				mapPathContent.put(path, content);
			}
		} catch (IOException e) {
			throw new WebjarLoadingException("Failed to load webjar content", e);
		}
	}

	private boolean isWebjarResource(JarEntry jarEntry) {
		if (jarEntry.isDirectory()) {
			return false;
		}

		if (!jarEntry.getName().startsWith(WEBJAR_CONTENT_PREFIX)) {
			return false;
		}

		return true;
	}

	private String jarEntryAsPath(JarEntry entry) {
		return entry.getName().substring(WEBJAR_CONTENT_PREFIX.length());
	}

	private String getCurrentEntryContent(JarInputStream jarInputStream) throws IOException {
		byte[] entryBuffer = new byte[1024];
		int len;

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		while ((len = jarInputStream.read(entryBuffer)) > 0) {
			outputStream.write(entryBuffer, 0, len);
		}

		return outputStream.toString("UTF-8");
	}

	public Set<String> getPaths() {
		return Collections.unmodifiableSet(mapPathContent.keySet());
	}

	public String getContent(String path) {
		return mapPathContent.get(path);
	}

	public static class WebjarLoadingException extends RuntimeException {
		public WebjarLoadingException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
