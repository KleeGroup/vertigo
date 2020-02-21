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
package io.vertigo.studio.plugins.mda.task.test;

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;

/**
 * Template used by freemarker.
 *
 * @author sezratty
 */
public final class TemplateAoTaskTest {
	private final String packageName;
	private final String classSimpleName;
	private final TemplateTaskDefinition templateTaskDefinition;
	private final String daoTestBaseClass;

	/**
	 * Constructor.
	 */
	TemplateAoTaskTest(
			final FileGeneratorConfig taskConfiguration,
			final TaskDefinition taskDefinition,
			final String packageName,
			final String classSimpleName,
			final String daoTestBaseClass) {
		Assertion.checkNotNull(taskConfiguration);
		Assertion.checkNotNull(taskDefinition);
		Assertion.checkNotNull(packageName);
		//-----
		this.packageName = packageName;

		this.classSimpleName = classSimpleName;
		this.templateTaskDefinition = new TemplateTaskDefinition(taskDefinition, packageName, classSimpleName);
		this.daoTestBaseClass = daoTestBaseClass;
	}

	/**
	 * @return Simple Nom (i.e. sans le package) de la classe de test.
	 */
	public String getClassSimpleName() {
		return classSimpleName;
	}

	/**
	 * @return Task
	 */
	public TemplateTaskDefinition getTaskDefinition() {
		return templateTaskDefinition;
	}

	/**
	 * @return Nom du package de la classe de DAO.
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return Nom canonique de la classe de base pour le test de DAO.
	 */
	public String getDaoTestBaseClass() {
		return daoTestBaseClass;
	}

	/**
	 * @return Nom simple de la classe de base pour le test de DAO.
	 */
	public String getDaoTestBaseClassSimpleName() {
		return getLastPackagename(daoTestBaseClass);
	}

	private static String getLastPackagename(final String canonicalName) {
		final String[] parts = canonicalName.split("\\.");
		return parts[parts.length - 1];
	}
}
