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
package io.vertigo.studio.impl.mda;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.core.param.ParamValue;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.mda.MdaManager;
import io.vertigo.studio.mda.MdaResult;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;

/**
 * Implements MDA.
 *
 * @author pchretien, dchallas
 */
public final class MdaManagerImpl implements MdaManager {
	private final List<GeneratorPlugin> generatorPlugins;
	private final FileGeneratorConfig fileGeneratorConfig;

	/**
	 * Constructor.
	 * @param generatorPlugins
	 * @param targetGenDirOpt Répertoire des fichiers TOUJOURS générés
	 * @param projectPackageName Racine du projet.
	 * @param encodingOpt Encoding des fichiers générés.
	 */
	@Inject
	public MdaManagerImpl(
			final List<GeneratorPlugin> generatorPlugins,
			@ParamValue("targetGenDir") final Optional<String> targetGenDirOpt,
			@ParamValue("projectPackageName") final String projectPackageName,
			@ParamValue("encoding") final Optional<String> encodingOpt) {
		Assertion.checkNotNull(generatorPlugins);
		Assertion.checkArgNotEmpty(projectPackageName);
		//-----
		this.generatorPlugins = java.util.Collections.unmodifiableList(generatorPlugins);
		final String targetGenDir = targetGenDirOpt.orElse("src/main/");
		final String encoding = encodingOpt.orElse("UTF-8");
		fileGeneratorConfig = new FileGeneratorConfig(targetGenDir, projectPackageName, encoding);
	}

	/** {@inheritDoc} */
	@Override
	public MdaResult generate() {
		//Création d'un objet listant les résultats
		final MdaResultBuilder mdaResultBuilder = MdaResult.builder();
		//Génèration des objets issus de la modélisation
		for (final GeneratorPlugin generatorPlugin : generatorPlugins) {
			generatorPlugin.generate(fileGeneratorConfig, mdaResultBuilder);
		}
		return mdaResultBuilder.build();
	}

	@Override
	public MdaResult clean() {
		final File directory = new File(fileGeneratorConfig.getTargetGenDir());
		Assertion.checkArgument(directory.exists(), "targetGenDir must exist");
		Assertion.checkArgument(directory.isDirectory(), "targetGenDir must be a directory");
		//---
		// We want to final clean the directory
		final MdaResultBuilder mdaResultBuilder = MdaResult.builder();
		for (final GeneratorPlugin generatorPlugin : generatorPlugins) {
			generatorPlugin.clean(fileGeneratorConfig, mdaResultBuilder);
		}
		return mdaResultBuilder.build();
	}

}
