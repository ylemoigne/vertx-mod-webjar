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

public class WebjarModuleException extends RuntimeException {
	public WebjarModuleException(String message) {
		super(message);
	}

	public WebjarModuleException(String message, Throwable cause) {
		super(message, cause);
	}
}
