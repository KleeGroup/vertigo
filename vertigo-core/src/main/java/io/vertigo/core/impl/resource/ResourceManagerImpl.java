/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.impl.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.daemon.DaemonScheduled;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.core.util.TempFile;

/**
 * Standard implementation for the resourceManager.
 * The strategy to access resources is defined by plugins.
 *
 * So, you can extend the capabilities and define your own plugin to be able to access your own resources wherever they are.
 *
 * @author pchretien
 */
public final class ResourceManagerImpl implements ResourceManager {

	private static final Logger LOG = LogManager.getLogger(ResourceManagerImpl.class);

	private final List<ResourceResolverPlugin> resourceResolverPlugins;

	/**
	 * Constructor.
	 * @param resourceResolverPlugins List of plugins  which resolve the resources.
	 */
	@Inject
	public ResourceManagerImpl(final List<ResourceResolverPlugin> resourceResolverPlugins) {
		Assertion.check().isNotNull(resourceResolverPlugins);
		//-----
		this.resourceResolverPlugins = resourceResolverPlugins;
		//-----
		final File documentRootFile = TempFile.VERTIGO_TMP_DIR_PATH.toFile();
		Assertion.check()
				.isTrue(documentRootFile.exists(), "Vertigo temp dir doesn't exists ({0})", TempFile.VERTIGO_TMP_DIR_PATH)
				.isTrue(documentRootFile.canRead(), "Vertigo temp dir can't be read ({0})", TempFile.VERTIGO_TMP_DIR_PATH)
				.isTrue(documentRootFile.canWrite(), "Vertigo temp dir can't be write ({0})", TempFile.VERTIGO_TMP_DIR_PATH);

	}

	/** {@inheritDoc} */
	@Override
	public URL resolve(final String resource) {
		return resourceResolverPlugins.stream()
				.map(resourceResolverPlugin -> resourceResolverPlugin.resolve(resource))
				.filter(Optional::isPresent)
				.map(Optional::get)
				/* We take the first url found.*/
				.findFirst()
				/* If we have not found any resolver for this resource we throw an exception*/
				.orElseThrow(() -> new VSystemException("Resource '{0}' not found", resource));
	}

	// Purge of TempFiles

	/**
	 * Daemon for deleting old files.
	 */
	@DaemonScheduled(name = "DmnPurgeTempFile", periodInSeconds = 5 * 60)
	public void deleteOldFiles() {
		final Path documentRootFile = TempFile.VERTIGO_TMP_DIR_PATH;
		final long maxTime = System.currentTimeMillis() - 60 * 60L * 1000L;
		if (Files.exists(documentRootFile)) {
			doDeleteOldFiles(documentRootFile, maxTime);
		}
	}

	private static void doDeleteOldFiles(final Path documentRootFile, final long maxTime) {
		final List<RuntimeException> processIOExceptions = new ArrayList<>();
		try (Stream<Path> fileStream = Files.list(documentRootFile)) {
			fileStream.forEach(subFile -> {
				if (Files.isDirectory(subFile) && Files.isReadable(subFile)) { //canRead pour les pbs de droits
					doDeleteOldFiles(subFile, maxTime);
				} else {
					boolean shouldDelete = false;
					try {
						shouldDelete = Files.getLastModifiedTime(subFile).toMillis() <= maxTime;
						if (shouldDelete) {
							Files.delete(subFile);
						}
					} catch (final IOException e) {
						managedIOException(processIOExceptions, e);
						if (shouldDelete) {
							subFile.toFile().deleteOnExit();
						}
					}
				}
			});
		} catch (final IOException e) {
			managedIOException(processIOExceptions, e);
		}
		if (!processIOExceptions.isEmpty()) {
			throw processIOExceptions.get(0); //We throw the first exception (for daemon health stats), and log the others
		}
	}

	private static void managedIOException(final List<RuntimeException> processIOExceptions, final IOException causeException) {
		processIOExceptions.add(WrappedException.wrap(causeException));
		LOG.error("doDeleteOldFiles error", causeException);
	}
}
