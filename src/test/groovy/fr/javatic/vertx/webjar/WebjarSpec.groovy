/*
 * ******************************************************************************
 *  Copyright (c) 2013 JavaTIC.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Yann Le Moigne - initial API and implementation
 * *****************************************************************************
 */

package fr.javatic.vertx.webjar

import org.vertx.java.core.buffer.Buffer
import spock.lang.Specification

class WebjarSpec extends Specification {
	def "Webjar sould not throw error when loading a correct jar"() {
		when:
		new Webjar(new Buffer(WebjarSpec.class.getResourceAsStream("/testWebJar.jar").getBytes()))

		then:
		noExceptionThrown()
	}

	def "Webjar should return as path of files contained in 'META-INF/resources/'"() {
		setup:
		def webjar = new Webjar(new Buffer(WebjarSpec.class.getResourceAsStream("/testWebJar.jar").getBytes()))

		when:
		def paths = webjar.paths

		then:
		paths == ['folderB/FileB.txt', 'folderA/FileA.txt'] as Set
	}

	def "Webjar sould return content for a given path"() {
		setup:
		def webjar = new Webjar(new Buffer(WebjarSpec.class.getResourceAsStream("/testWebJar.jar").getBytes()))

		when:
		def content = webjar.getContent('folderB/FileB.txt')

		then:
		content == '''Hello from FileB !\r\nYeah it's another file...'''
	}
}
