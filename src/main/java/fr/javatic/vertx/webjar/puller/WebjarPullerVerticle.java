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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import fr.javatic.vertx.webjar.WebjarModuleConfig;
import fr.javatic.vertx.webjar.WebjarModuleException;

public class WebjarPullerVerticle extends Verticle {
	private FileSystem fileSystem;
	private JsonObject config;

	@Override
	public void start() {
		config = container.config();
		fileSystem = vertx.fileSystem();

		final String webjarsDirPath = config.getString(WebjarModuleConfig.WEBJARS_DIR_PATH,
				WebjarModuleConfig.WEBJARS_DIR_PATH_DEFAULT);
		final JsonArray webjars = config.getArray(WebjarModuleConfig.WEBJARS, WebjarModuleConfig.WEBJARS_DEFAULT);

		fileSystem.mkdirSync(webjarsDirPath, true);

		List<Dependency> dependencies = getDependencies(webjars);
		Collection<File> artifacts = getArtifactsFiles(dependencies);

		for (File artifact : artifacts) {
			String artifactPath = artifact.getAbsolutePath();
			String webjarPath = webjarsDirPath + "/" + artifact.getName();

			if (fileSystem.existsSync(webjarPath)) {
				fileSystem.deleteSync(webjarPath);
			}

			fileSystem.copySync(artifactPath, webjarPath);
		}
	}

	private Collection<File> getArtifactsFiles(List<Dependency> dependencies) {
		String reposFileName = config.getString(WebjarModuleConfig.REPOS_FILE, WebjarModuleConfig.REPOS_FILE_DEFAULT);

		RepositoryConfig repositoryConfig = new RepositoryConfig(container.logger(), reposFileName);

		RepositorySystem system = createRepositorySystem();
		RepositorySystemSession session = createRepositorySystemSession(system, repositoryConfig.getLocalRepository());

		DependencyFilter classpathFilter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);

		CollectRequest collectRequest = new CollectRequest(dependencies, null, repositoryConfig.getRemoteRepositories());
		DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFilter);

		try {
			List<ArtifactResult> artifactResults = system.resolveDependencies(session, dependencyRequest)
					.getArtifactResults();

			Collection<File> artifactsFiles = new HashSet<>();
			for (ArtifactResult artifactResult : artifactResults) {
				checkArtifactResultStatus(artifactResult);
				artifactsFiles.add(artifactResult.getArtifact().getFile());
			}
			return artifactsFiles;
		} catch (DependencyResolutionException e) {
			throw new WebjarModuleException("Failed to resolve dependencies", e);
		}
	}

	private void checkArtifactResultStatus(ArtifactResult artifactResult) {
		if (!artifactResult.isResolved()) {
			throw new WebjarModuleException(String.format("Failed to get artifact (Result: %s)",
					artifactResult.toString()));
		}
	}

	private List<Dependency> getDependencies(JsonArray webjars) {
		List<Dependency> dependencies = new ArrayList<>();

		for (int i = 0; i < webjars.size(); i++) {
			String artifactGav = webjars.get(i);

			Artifact artifact = new DefaultArtifact(artifactGav);
			Dependency dependency = new Dependency(artifact, JavaScopes.COMPILE);
			dependencies.add(dependency);
		}

		return dependencies;
	}

	private RepositorySystem createRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

		return locator.getService(RepositorySystem.class);
	}

	private RepositorySystemSession createRepositorySystemSession(RepositorySystem system,
			LocalRepository localRepository) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepository));
		session.setTransferListener(new ConsoleTransferListener());
		session.setRepositoryListener(new ConsoleRepositoryListener());
		session.setReadOnly();
		return session;
	}
}
