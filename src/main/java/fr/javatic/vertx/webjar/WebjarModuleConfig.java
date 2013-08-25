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

import org.vertx.java.core.json.JsonArray;

public interface WebjarModuleConfig {
	public static final String REPOS_FILE = "repos-file";
	public static final String REPOS_FILE_DEFAULT = "repos.txt";

	public static final String WEBJARS_DIR_PATH = "webjars-dir";
	public static final String WEBJARS_DIR_PATH_DEFAULT = "webjars";

	public static final String WEBJARS = "webjars";
	public static final JsonArray WEBJARS_DEFAULT = new JsonArray();

	public static final String PULLIN_ON_STARTUP = "pullin-on-startup";
	public static final boolean PULLIN_ON_STARTUP_DEFAULT = false;
}
