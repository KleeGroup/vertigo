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
package io.vertigo.studio.plugins.mda.task.model;

import java.util.ArrayList;
import java.util.Collection;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.util.StringUtil;

/**
 * Objet utilisé par FreeMarker.
 *
 * @author pchretien
 */
public final class DAOModel {
	private final DtDefinition dtDefinition;
	private final String packageName;
	private final Collection<TaskDefinitionModel> taskDefinitions = new ArrayList<>();

	private final boolean hasOptions;

	/**
	 * Constructeur.
	 *
	 * @param dtDefinition DtDefinition de l'objet à générer
	 */
	public DAOModel(final FileGeneratorConfig fileGeneratorConfig, final DtDefinition dtDefinition, final Collection<TaskDefinition> taskDefinitionCollection) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(taskDefinitionCollection);
		final String definitionPackageName = dtDefinition.getPackageName();
		final String packageNamePrefix = fileGeneratorConfig.getProjectPackageName();
		// ---
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

		this.dtDefinition = dtDefinition;
		//On construit le nom du package à partir du package de la DT et de la feature.
		packageName = fileGeneratorConfig.getProjectPackageName() + featureName + ".dao" + subpackage;

		boolean hasOption = false;
		for (final TaskDefinition taskDefinition : taskDefinitionCollection) {
			final TaskDefinitionModel templateTaskDefinition = new TaskDefinitionModel(taskDefinition);
			taskDefinitions.add(templateTaskDefinition);
			hasOption = hasOption || templateTaskDefinition.hasOptions();
		}
		hasOptions = hasOption;
	}

	/**
	 * @return Simple Nom (i.e. sans le package) de la classe d'implémentation du DtObject
	 */
	public String getClassSimpleName() {
		return dtDefinition.getClassSimpleName() + "DAO";
	}

	/**
	 * @return Si l'entité est un keyConcept
	 */
	public boolean isKeyConcept() {
		return dtDefinition.getStereotype() == DtStereotype.KeyConcept;
	}

	/**
	 * @return Type de la PK
	 */
	public String getIdFieldType() {
		return dtDefinition.getIdField().get().getDomain().getJavaClass().getCanonicalName();
	}

	/**
	 * @return Nom de la classe du Dt
	 */
	public String getDtClassCanonicalName() {
		return dtDefinition.getClassCanonicalName();
	}

	/**
	 * @return Nom simple de la classe du Dt
	 */
	public String getDtClassSimpleName() {
		return dtDefinition.getClassSimpleName();
	}

	/**
	 * @return Nom du package
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return Liste des tasks
	 */
	public Collection<TaskDefinitionModel> getTaskDefinitions() {
		return taskDefinitions;
	}

	/**
	 * @return Si ce dao utilise au moins une option : vertigo.core.lang.Option
	 */
	public boolean isOptions() {
		return hasOptions;
	}

}
