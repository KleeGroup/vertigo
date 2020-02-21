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
package io.vertigo.studio.plugins.mda.domain.js;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.core.param.ParamValue;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.studio.plugins.mda.domain.js.model.JSDtDefinitionModel;
import io.vertigo.studio.plugins.mda.util.DomainUtil;
import io.vertigo.studio.plugins.mda.util.MdaUtil;
import io.vertigo.util.MapBuilder;

/**
 * Génération des objets relatifs au module Domain.
 *
 * @author pchretien
 */
public final class JSGeneratorPlugin implements GeneratorPlugin {
	private final String targetSubDir;
	private final boolean shouldGenerateDtResourcesJS;
	private final boolean shouldGenerateJsDtDefinitions;

	/**
	 * Constructeur.
	 * @param targetSubDirOpt Repertoire de generation des fichiers de ce plugin
	 * @param generateDtResourcesJSOpt Si on génère les fichiers i18n pour les labels des champs en JS
	 * @param generateJsDtDefinitionsOpt Si on génère les classes JS.
	 */
	@Inject
	public JSGeneratorPlugin(
			@ParamValue("targetSubDir") final Optional<String> targetSubDirOpt,
			@ParamValue("generateDtResourcesJS") final Optional<Boolean> generateDtResourcesJSOpt,
			@ParamValue("generateJsDtDefinitions") final Optional<Boolean> generateJsDtDefinitionsOpt) {
		//-----
		targetSubDir = targetSubDirOpt.orElse("jsgen");
		shouldGenerateDtResourcesJS = generateDtResourcesJSOpt.orElse(true); //true by default
		shouldGenerateJsDtDefinitions = generateJsDtDefinitionsOpt.orElse(true);//true by default
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(mdaResultBuilder);
		//-----
		/* Génération des ressources afférentes au DT mais pour la partie JS.*/
		if (shouldGenerateDtResourcesJS) {
			generateDtResourcesJS(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
		}
		/* Génération des fichiers javascripts référençant toutes les définitions. */
		if (shouldGenerateJsDtDefinitions) {
			generateJsDtDefinitions(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
		}
	}

	private static List<JSDtDefinitionModel> getJsDtDefinitionModels() {
		return DomainUtil.getDtDefinitions().stream()
				.map(JSDtDefinitionModel::new)
				.collect(Collectors.toList());
	}

	private static void generateJsDtDefinitions(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {

		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("packageName", fileGeneratorConfig.getProjectPackageName() + ".domain")
				.put("classSimpleName", "DtDefinitions")
				.put("dtDefinitions", getJsDtDefinitionModels())
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName("DtDefinitions.js")
				.withGenSubDir(targetSubDir)
				.withPackageName(fileGeneratorConfig.getProjectPackageName() + ".domain")
				.withTemplateName("domain/js/template/js.ftl")
				.build()
				.generateFile(mdaResultBuilder);
	}

	/**
	 * Génère les ressources JS pour les traductions.
	 * @param fileGeneratorConfig Configuration du domaine.
	 */
	private static void generateDtResourcesJS(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {
		final String simpleClassName = "DtDefinitions" + "Label";
		final String packageName = fileGeneratorConfig.getProjectPackageName() + ".domain";

		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("packageName", packageName)
				.put("simpleClassName", simpleClassName)
				.put("dtDefinitions", getJsDtDefinitionModels())
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName(simpleClassName + ".js")
				.withGenSubDir(targetSubDir)
				.withPackageName(packageName)
				.withTemplateName("domain/js/template/propertiesJS.ftl")
				.build()
				.generateFile(mdaResultBuilder);
	}

	@Override
	public void clean(final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		MdaUtil.deleteFiles(new File(fileGeneratorConfig.getTargetGenDir() + targetSubDir), mdaResultBuilder);
	}

}
