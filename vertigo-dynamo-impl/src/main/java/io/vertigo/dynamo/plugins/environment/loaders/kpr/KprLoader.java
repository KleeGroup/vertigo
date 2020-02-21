/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.dynamo.plugins.environment.loaders.kpr;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.core.param.ParamValue;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinitionRepository;
import io.vertigo.dynamo.plugins.environment.loaders.Loader;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.lang.WrappedException;

/**
 * Parses a KPR file.
 * A KPR file is a project file that lists all the KSP files.
 *
 * @author pchretien
 */
public final class KprLoader implements Loader {

	private static final String KPR_EXTENSION = ".kpr";
	private static final String KSP_EXTENSION = ".ksp";
	private final ResourceManager resourceManager;
	private final Charset charset;

	/**
	 * Constructor.
	 *
	 * @param resourceManager the resourceManager
	 * @param encoding encoding des KSP
	 */
	@Inject
	public KprLoader(
			final ResourceManager resourceManager,
			@ParamValue("encoding") final Optional<String> encoding) {
		Assertion.checkNotNull(resourceManager);
		Assertion.checkNotNull(encoding);
		//-----
		this.resourceManager = resourceManager;
		charset = Charset.forName(encoding.orElse("utf-8"));
	}

	/** {@inheritDoc} */
	@Override
	public void load(final String resourcePath, final DslDefinitionRepository dslDefinitionRepository) {
		Assertion.checkArgNotEmpty(resourcePath);
		Assertion.checkNotNull(dslDefinitionRepository);
		//-----
		final URL kprURL = resourceManager.resolve(resourcePath);
		for (final URL url : getKspFiles(kprURL, charset, resourceManager)) {
			final KspLoader loader = new KspLoader(url, charset);
			loader.load(dslDefinitionRepository);
		}
	}

	/**
	 * rÃ©cupÃ¨re la liste des fichiers KSP correspondant Ã  un KPR.
	 *
	 * @param kprURL fichier KPR
	 * @return List liste des fichiers KSP.
	 */
	private static List<URL> getKspFiles(final URL kprURL, final Charset charset, final ResourceManager resourceManager) {
		try {
			return doGetKspFiles(kprURL, charset, resourceManager);
		} catch (final Exception e) {
			throw WrappedException.wrap(e, "Echec de lecture du fichier KPR {0}", kprURL.getFile());
		}
	}

	private static List<URL> doGetKspFiles(final URL kprURL, final Charset charset, final ResourceManager resourceManager) throws Exception {
		final List<URL> kspFiles = new ArrayList<>();
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(kprURL.openStream(), charset))) {
			String path = kprURL.getPath();
			path = path.substring(0, path.lastIndexOf('/'));

			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				final String fileName = line.trim();
				if (fileName.length() > 0) {
					// voir http://commons.apache.org/vfs/filesystems.html
					// Protocol : vfszip pour jboss par exemple
					final URL url = new URL(kprURL.getProtocol() + ':' + path + '/' + fileName);
					if (fileName.endsWith(KPR_EXTENSION)) {
						// kpr
						kspFiles.addAll(getKspFiles(url, charset, resourceManager));
					} else if (fileName.endsWith(KSP_EXTENSION)) {
						// ksp
						kspFiles.add(url);
					} else {
						throw new VSystemException("Type de fichier inconnu : {0}", fileName);
					}
				}
			}
		}
		return kspFiles;
	}

	/** {@inheritDoc} */
	@Override
	public String getType() {
		return "kpr";
	}
}
