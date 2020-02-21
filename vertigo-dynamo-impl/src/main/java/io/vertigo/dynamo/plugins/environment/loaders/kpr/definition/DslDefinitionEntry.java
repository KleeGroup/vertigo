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
package io.vertigo.dynamo.plugins.environment.loaders.kpr.definition;

import java.util.List;

import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinition;
import io.vertigo.lang.Assertion;

/**
 * Une entrée de type définition est définie (XOR)
 * - soit par un champ et une définition,
 * - soit par un champ et une liste de clés de définition.
 *
 * @author pchretien
 */
public final class DslDefinitionEntry {
	/**
	 * Obligatoire
	 */
	private final String fieldName;
	private final DslDefinition definition;
	private final List<String> definitionNames;

	/**
	 * Constructor.
	 *
	 * @param definitionNames Liste des clés de définition
	 * @param fieldName Nom du champ
	 */
	public DslDefinitionEntry(final String fieldName, final List<String> definitionNames) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(definitionNames);
		//-----
		this.fieldName = fieldName;
		definition = null;
		this.definitionNames = definitionNames;
	}

	/**
	 * Constructor.
	 *
	 * @param fieldName Nom du champ
	 * @param definition Définition
	 */
	public DslDefinitionEntry(final String fieldName, final DslDefinition definition) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(definition);
		//-----
		this.fieldName = fieldName;
		this.definition = definition;
		definitionNames = null;
	}

	/**
	 * @return Nom du champ
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Permet de savoir dans quel mode on se trouve (Definition XOR List<DefinitionKey>).
	 *
	 * @return boolean
	 */
	public boolean containsDefinition() {
		return definition != null;
	}

	/**
	 * @return Définition
	 */
	public DslDefinition getDefinition() {
		Assertion.checkNotNull(definition);
		//-----
		return definition;
	}

	/**
	 * @return List des clés de définition
	 */
	public List<String> getDefinitionNames() {
		return definitionNames;
	}
}
