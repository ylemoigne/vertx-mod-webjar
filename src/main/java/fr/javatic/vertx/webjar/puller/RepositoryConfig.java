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

package fr.javatic.vertx.webjar.puller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.vertx.java.core.logging.Logger;

public class RepositoryConfig {
	private LocalRepository localRepository;
	private List<RemoteRepository> remoteRepositories;

	public RepositoryConfig(Logger logger, String reposFileName) {
		remoteRepositories = new ArrayList<>();

		try (InputStream is = getClass().getClassLoader().getResourceAsStream(reposFileName)) {
			if (is != null) {
				BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
				String line;
				while ((line = rdr.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						// blank line or comment
						continue;
					}
					int colonPos = line.indexOf(':');
					if (colonPos == -1 || colonPos == line.length() - 1) {
						throw new IllegalArgumentException("Invalid repo: " + line);
					}
					String type = line.substring(0, colonPos);
					String repoID = line.substring(colonPos + 1);
					switch (type) {
						case "mavenLocal":
							localRepository = new LocalRepository(expandPath(repoID));
							break;
						case "maven":
							remoteRepositories.add(new RemoteRepository.Builder(UUID.randomUUID().toString(),
									"default", repoID).build());
							break;
						default:
							logger.info(String.format("%s repository are not supported webjar, will ignore '%s'", type,
									repoID));
					}
				}
			}
		} catch (IOException e) {
			throw new RepositoryConfigException("Failed to load " + reposFileName + " " + e.getMessage(), e);
		}
	}

	public LocalRepository getLocalRepository() {
		return localRepository;
	}

	public List<RemoteRepository> getRemoteRepositories() {
		return Collections.unmodifiableList(remoteRepositories);
	}

	private String expandPath(String path) {
		return path.replace("~", System.getProperty("user.home"));
	}
}
