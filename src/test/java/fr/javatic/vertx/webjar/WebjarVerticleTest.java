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

import static org.junit.Assert.assertFalse;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

public class WebjarVerticleTest extends TestVerticle {
	private final static String WEBJARS_DIR = "testWebjar";

	@Test
	public void testContent() {
		vertx.eventBus().send("module.webjar.resourceRequest", "webjars/angularjs/1.0.7/angular.js",
				new Handler<Message<String>>() {
					@Override
					public void handle(Message<String> event) {
						String content = event.body();
						assertNotNull(content);
						assertFalse(content.isEmpty());
						testComplete();
					}
				});
	}

	@Override
	public void start() {
		initialize();
		cleanWebjarsDir();

		JsonObject config = new JsonObject();

		JsonArray webjars = new JsonArray();
		webjars.addString("org.webjars:angularjs:1.0.7");
		config.putArray("webjars", webjars);
		config.putBoolean("pullin-on-startup", true);
		config.putString("webjars-dir", WEBJARS_DIR);

		container.deployVerticle(WebjarVerticle.class.getName(), config, new AsyncResultHandler<String>() {
			@Override
			public void handle(AsyncResult<String> asyncResult) {
				if (asyncResult.failed()) {
					container.logger().error(asyncResult.cause());
				}
				assertTrue(asyncResult.succeeded());
				assertNotNull("deploymentID should not be null", asyncResult.result());
				startTests();
			}
		});
	}

	@Override
	public void stop() {
		cleanWebjarsDir();
	}

	private void cleanWebjarsDir() {
		FileSystem fileSystem = vertx.fileSystem();
		if (fileSystem.existsSync(WEBJARS_DIR)) {
			fileSystem.deleteSync(WEBJARS_DIR, true);
		}
	}
}
