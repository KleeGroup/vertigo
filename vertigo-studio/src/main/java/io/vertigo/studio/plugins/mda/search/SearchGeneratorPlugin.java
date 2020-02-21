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
package io.vertigo.studio.plugins.mda.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.core.param.ParamValue;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.studio.plugins.mda.search.model.FacetedQueryDefinitionModel;
import io.vertigo.studio.plugins.mda.search.model.SearchDtDefinitionModel;
import io.vertigo.studio.plugins.mda.util.MdaUtil;
import io.vertigo.util.MapBuilder;
import io.vertigo.util.StringUtil;

/**
 * Génération des objets relatifs au module Task.
 *
 * @author pchretien
 */
public final class SearchGeneratorPlugin implements GeneratorPlugin {

	private final String targetSubDir;

	/**
	 * Constructeur.
	 * @param targetSubDirOpt Repertoire de generation des fichiers de ce plugin
	 */
	@Inject
	public SearchGeneratorPlugin(@ParamValue("targetSubDir") final Optional<String> targetSubDirOpt) {
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
		generateSearchAos(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
	}

	/**
	 * Génération de tous les PAOs.
	 */
	private static void generateSearchAos(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {

		Home.getApp().getDefinitionSpace().getAll(DtDefinition.class)
				.stream()
				.filter(dtDefinition -> dtDefinition.getStereotype() == DtStereotype.KeyConcept)
				.forEach(dtDefinition -> generateSearchAo(targetSubDir, fileGeneratorConfig, mdaResultBuilder, dtDefinition));

	}

	/**
	 * Génération d'un DAO c'est à dire des taches afférentes à un objet.
	 */
	private static void generateSearchAo(
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder,
			final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);

		final String definitionPackageName = dtDefinition.getPackageName();
		final String packageNamePrefix = fileGeneratorConfig.getProjectPackageName();
		Assertion.checkArgument(definitionPackageName.startsWith(packageNamePrefix), "Package name {0}, must begin with normalised prefix: {1}", definitionPackageName, packageNamePrefix);
		Assertion.checkArgument(definitionPackageName.substring(packageNamePrefix.length()).contains(".domain"), "Package name {0}, must contains the modifier .domain", definitionPackageName);
		// ---
		//we need to find the featureName, aka between projectpackageName and .domain
		final String featureName = definitionPackageName.substring(packageNamePrefix.length(), definitionPackageName.indexOf(".domain"));
		if (!StringUtil.isEmpty(featureName)) {
			Assertion.checkState(featureName.lastIndexOf('.') == 0, "The feature {0} must not contain any dot", featureName.substring(1));
		}
		// the subpackage is what's behind the .domain
		final String subpackage = definitionPackageName.substring(definitionPackageName.indexOf(".domain") + ".domain".length());
		// breaking change -> need to redefine what's the desired folder structure in javagen...

		//On construit le nom du package à partir du package de la DT et de la feature.
		final String packageName = fileGeneratorConfig.getProjectPackageName() + featureName + ".search" + subpackage;

		final List<FacetedQueryDefinitionModel> facetedQueryDefinitions = new ArrayList<>();
		final SearchDtDefinitionModel searchDtDefinitionModel = new SearchDtDefinitionModel(dtDefinition);
		for (final FacetedQueryDefinition facetedQueryDefinition : Home.getApp().getDefinitionSpace().getAll(FacetedQueryDefinition.class)) {
			if (facetedQueryDefinition.getKeyConceptDtDefinition().equals(dtDefinition)) {
				final FacetedQueryDefinitionModel templateFacetedQueryDefinition = new FacetedQueryDefinitionModel(facetedQueryDefinition);
				facetedQueryDefinitions.add(templateFacetedQueryDefinition);
			}
		}

		final Optional<SearchIndexDefinition> searchIndexDefinitionOpt = Home.getApp().getDefinitionSpace().getAll(SearchIndexDefinition.class).stream()
				.filter(indexDefinition -> indexDefinition.getKeyConceptDtDefinition().equals(dtDefinition))
				.findFirst();

		if (searchIndexDefinitionOpt.isPresent()) {

			final Map<String, Object> model = new MapBuilder<String, Object>()
					.put("packageName", packageName)
					.put("facetedQueryDefinitions", facetedQueryDefinitions)
					.put("dtDefinition", searchDtDefinitionModel)
					.put("indexDtDefinition", new SearchDtDefinitionModel(searchIndexDefinitionOpt.get().getIndexDtDefinition()))
					.build();

			FileGenerator.builder(fileGeneratorConfig)
					.withModel(model)
					.withFileName(searchDtDefinitionModel.getClassSimpleName() + "SearchClient.java")
					.withGenSubDir(targetSubDir)
					.withPackageName(packageName)
					.withTemplateName("search/template/search_client.ftl")
					.build()
					.generateFile(mdaResultBuilder);
		}
	}

	@Override
	public void clean(final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		MdaUtil.deleteFiles(new File(fileGeneratorConfig.getTargetGenDir() + targetSubDir), mdaResultBuilder);
	}
}
