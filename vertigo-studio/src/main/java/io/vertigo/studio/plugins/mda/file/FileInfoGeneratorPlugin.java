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
package io.vertigo.studio.plugins.mda.file;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.core.param.ParamValue;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.studio.plugins.mda.file.model.FileInfoDefinitionModel;
import io.vertigo.studio.plugins.mda.util.MdaUtil;
import io.vertigo.util.MapBuilder;

/**
 * Génération des objets relatifs au module File.
 *
 * @author npiedeloup
 */
public final class FileInfoGeneratorPlugin implements GeneratorPlugin {
	private final String targetSubDir;

	/**
	 * Constructeur.
	 * @param targetSubDirOpt Repertoire de generation des fichiers de ce plugin
	 */
	@Inject
	public FileInfoGeneratorPlugin(@ParamValue("targetSubDir") final Optional<String> targetSubDirOpt) {
		//-----
		targetSubDir = targetSubDirOpt.orElse("javagen");
	}

	/** {@inheritDoc} */
	@Override
	public void generate(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(mdaResultBuilder);
		//-----
		/* Générations des FI. */
		generateFileInfos(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
	}

	private static void generateFileInfos(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {
		final Collection<FileInfoDefinition> fileInfoDefinitions = Home.getApp().getDefinitionSpace().getAll(FileInfoDefinition.class);
		for (final FileInfoDefinition fileInfoDefinition : fileInfoDefinitions) {
			generateFileInfo(targetSubDir, fileGeneratorConfig, mdaResultBuilder, fileInfoDefinition);
		}
	}

	private static void generateFileInfo(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder,
			final FileInfoDefinition fileInfoDefinition) {
		final FileInfoDefinitionModel fileInfoDefinitionModel = new FileInfoDefinitionModel(fileInfoDefinition);

		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("fiDefinition", fileInfoDefinitionModel)
				.put("packageName", fileGeneratorConfig.getProjectPackageName() + ".fileinfo")
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName(fileInfoDefinitionModel.getClassSimpleName() + ".java")
				.withGenSubDir(targetSubDir)
				.withPackageName(fileGeneratorConfig.getProjectPackageName() + ".fileinfo")
				.withTemplateName("file/template/fileInfo.ftl")
				.build()
				.generateFile(mdaResultBuilder);
	}

	@Override
	public void clean(final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		MdaUtil.deleteFiles(new File(fileGeneratorConfig.getTargetGenDir() + targetSubDir), mdaResultBuilder);
	}
}
