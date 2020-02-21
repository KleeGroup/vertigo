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
package io.vertigo.dynamo.plugins.environment.dsl.entity;

import java.util.Collections;
import java.util.List;

import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinition;

/**
 * Une grammaire est composée d'entités et de propriétés.
 * Les entités sont une composition d'entités et de propriétés.
 *
 * Il est possible de composer une grammaire à partir de grammaires.
 *
 * @author pchretien
 */
@FunctionalInterface
public interface DslGrammar {
	/**
	 * Returns the list of entities
	 * @return List of entities.
	 */
	List<DslEntity> getEntities();

	/**
	 * Examples : a grammar is defined from atoms (string...)
	 * @return the list of primitives/ root definitions
	 */
	default List<DslDefinition> getRootDefinitions() {
		return Collections.emptyList();
	}
}
