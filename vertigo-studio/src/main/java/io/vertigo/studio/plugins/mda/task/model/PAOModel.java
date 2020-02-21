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
import java.util.Collections;

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.util.StringUtil;

/**
 * Objet utilisé par FreeMarker.
 *
 * @author pchretien
 */
public final class PAOModel {
	private final String packageName;
	private final String className;
	private final Collection<TaskDefinitionModel> taskDefinitionModels;
	private final boolean hasOptions;

	/**
	 * Constructor.
	 */
	public PAOModel(final FileGeneratorConfig fileGeneratorConfig, final Collection<TaskDefinition> taskDefinitionCollection, final String packageName) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(taskDefinitionCollection);
		Assertion.checkArgument(!taskDefinitionCollection.isEmpty(), "Aucune tache dans le package {0}", packageName);
		Assertion.checkNotNull(packageName);
		//-----
		this.packageName = packageName;

		className = getLastPackagename(packageName) + "PAO";
		boolean hasOption = false;
		taskDefinitionModels = new ArrayList<>();
		for (final TaskDefinition taskDefinition : taskDefinitionCollection) {
			final TaskDefinitionModel templateTaskDefinition = new TaskDefinitionModel(taskDefinition);
			taskDefinitionModels.add(templateTaskDefinition);
			hasOption = hasOption || templateTaskDefinition.hasOptions();
		}
		hasOptions = hasOption;
	}

	/**
	 * @return Simple Nom (i.e. sans le package) de la classe d'implémentation du DtObject
	 */
	public String getClassSimpleName() {
		return className;
	}

	/**
	 * @return Liste des tasks
	 */
	public Collection<TaskDefinitionModel> getTaskDefinitions() {
		return Collections.unmodifiableCollection(taskDefinitionModels);
	}

	/**
	 * @return Nom du package.
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * Retourne le nom du package feuille à partir d'un nom complet de package.
	 * exemple : org.company.sugar > sugar
	 * @param packageName Nom de package
	 * @return Nom du package feuille à partir d'un nom complet de package
	 */
	private static String getLastPackagename(final String packageName) {
		String lastPackageName = packageName;
		if (lastPackageName.indexOf('.') != -1) {
			lastPackageName = lastPackageName.substring(lastPackageName.lastIndexOf('.') + 1);
		}
		return StringUtil.first2UpperCase(lastPackageName);
	}

	/**
	 * @return Si ce pao utilise au moins une Option : vertigo.core.lang.Option
	 */
	public boolean isOptions() {
		return hasOptions;
	}
}
