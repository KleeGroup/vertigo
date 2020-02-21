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

import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Génération des classes/méthodes des taches de type DAO.
 *
 * @author sezratty, mlaroche
 */
public final class TemplateTaskDefinition {
	private final TaskDefinition taskDefinition;
	private final List<TemplateTaskAttribute> templateInTaskAttributes;
	private final String testPackageName;
	private final String testClassSimpleName;
	private final String packageName;
	private final String classSimpleName;

	TemplateTaskDefinition(final TaskDefinition taskDefinition, final String packageName, final String classSimpleName) {
		Assertion.checkNotNull(taskDefinition);
		Assertion.checkNotNull(packageName);
		Assertion.checkNotNull(classSimpleName);
		//-----
		this.taskDefinition = taskDefinition;
		this.packageName = packageName;
		this.classSimpleName = classSimpleName;

		testPackageName = this.packageName + "." + StringUtil.first2LowerCase(this.classSimpleName) + "Test";
		testClassSimpleName = StringUtil.first2UpperCase(getMethodName()) + "Test";

		// Paramètres in
		templateInTaskAttributes = taskDefinition.getInAttributes().stream()
				.map(taskAttribute -> new TemplateTaskAttribute(taskAttribute))
				.collect(Collectors.toList());
	}

	/**
	 * @return Nom de la méthode en CamelCase
	 */
	public String getMethodName() {
		final String localName = DefinitionUtil.getLocalName(taskDefinition.getName(), TaskDefinition.class);
		return StringUtil.first2LowerCase(localName);
	}

	/**
	 * @return Nom du package de test en cascalCase
	 */
	public String getTestPackageName() {
		return testPackageName;
	}

	/**
	 * @return Nom simple de la classe de test en PascalCase
	 */
	public String getTestClassName() {
		return testClassSimpleName;
	}

	/**
	 * @return Nom canonique de la classe de test
	 */
	public String getTestClassCanonicalName() {
		return testPackageName + "." + testClassSimpleName;
	}

	/**
	 * @return Nom cannonique  (i.e. avec le package) de la classe d'implémentation du DAO
	 */
	public String getClassName() {
		return packageName + "." + classSimpleName;
	}

	/**
	 * @return Nom de la variable PAO dans le test.
	 */
	public String getDaoVariable() {
		return StringUtil.first2LowerCase(classSimpleName);
	}

	/**
	 * @return Nom de la méthode de test en CamelCase
	 */
	public String getTestMethodName() {
		return "check_" + getMethodName() + "_Ok";
	}

	/**
	 * @return Liste des attributs en entréee
	 */
	public List<TemplateTaskAttribute> getInAttributes() {
		return templateInTaskAttributes;
	}

}
