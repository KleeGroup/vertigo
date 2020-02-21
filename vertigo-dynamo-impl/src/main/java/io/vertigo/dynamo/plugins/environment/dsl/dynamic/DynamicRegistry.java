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
package io.vertigo.dynamo.plugins.environment.dsl.dynamic;

import java.util.Collections;
import java.util.List;

import io.vertigo.core.definition.DefinitionSupplier;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslGrammar;

/**
 * This handler creates
 * - creates a definition from a DynamicDefinition
 * - adds dslDefinition from a new DynamicDefinition
 *
 * example : Each time a DtDefinition, two others definitions (domains)  are created (a domain for one object, a domain for a list).
 * @author pchretien
 */
public interface DynamicRegistry {
	/**
	 * @return Grammar
	 */
	DslGrammar getGrammar();

	/**
	 * Create a definition from a dynamic definition in a context defined by definitionSpace (preexisting definitions).
	 * @param definitionSpace Space where all the definitions are stored.
	 * @param dslDefinition Definition
	 * @return An optional definition
	 */
	DefinitionSupplier supplyDefinition(DslDefinition dslDefinition);

	/**
	 * Ajout d'une définition.
	 * Utilisé pour créer des définitions Ã  partir d'autres Definitions.
	 * Exemple : création des domaines à partir d'un DT.
	 *
	 * @param dslDefinition dslDefinition
	 * @return a list of dslDefinitions
	 */
	default List<DslDefinition> onNewDefinition(final DslDefinition dslDefinition) {
		return Collections.emptyList();
	}
}
