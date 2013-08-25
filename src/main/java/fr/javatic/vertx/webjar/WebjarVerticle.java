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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import fr.javatic.vertx.webjar.puller.WebjarPullerVerticle;

public class WebjarVerticle extends Verticle {
	public final static String WEBJAR_ADDRESS = "module.webjar.resourceRequest";

	private Map<String, String> mapPathContent;

	private FileSystem fileSystem;
	private EventBus eventBus;
	private JsonObject config;

	@Override
	public void start(final Future<Void> startedResult) {
		fileSystem = vertx.fileSystem();
		eventBus = vertx.eventBus();
		config = container.config();

		mapPathContent = new ConcurrentHashMap<>();

		registerOnEventBus();

		boolean pullInOnStartup = config.getBoolean(WebjarModuleConfig.PULLIN_ON_STARTUP,
				WebjarModuleConfig.PULLIN_ON_STARTUP_DEFAULT);
		if (pullInOnStartup) {
			container.deployWorkerVerticle(WebjarPullerVerticle.class.getName(), config, 1, false,
					new AsyncResultHandler<String>() {
						@Override
						public void handle(AsyncResult<String> event) {
							if (event.failed()) {
								startedResult.setFailure(event.cause());
							}

							loadWebjars();
							startedResult.setResult(null);
						}
					});
		} else {
			loadWebjars();
			startedResult.setResult(null);
		}
	}

	private void registerOnEventBus() {
		eventBus.registerHandler(WEBJAR_ADDRESS, new Handler<Message>() {
			@Override
			public void handle(Message event) {
				String path = (String) event.body();
				String content = mapPathContent.get(path);

				event.reply(content);
			}
		});
	}

	private void loadWebjars() {
		final String webjarsDirectoryPath = config.getString(WebjarModuleConfig.WEBJARS_DIR_PATH,
				WebjarModuleConfig.WEBJARS_DIR_PATH_DEFAULT);

		String[] webjarsPaths = fileSystem.readDirSync(webjarsDirectoryPath);
		for (String webjarPath : webjarsPaths) {
			loadWebjar(webjarPath);
		}
	}

	private void loadWebjar(final String webjarPath) {
		Buffer buffer = fileSystem.readFileSync(webjarPath);
		Webjar webJar = new Webjar(buffer);

		for (String path : webJar.getPaths()) {
			mapPathContent.put(path, webJar.getContent(path));
		}
	}
}
