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
package io.vertigo.core.definition;

import java.util.List;

import io.vertigo.app.config.DefinitionResourceConfig;

/**
 * Provides a list of definitions through an iterable.
 * @author pchretien
 *
 */
public interface DefinitionProvider {

	/**
	 * Return a list of definitions with a set of already known definitions
	 * @param definitionSpace the actual definitionSpace with a readOnly access
	 * @return the list of new definition to register
	 */
	List<DefinitionSupplier> get(final DefinitionSpace definitionSpace);

	/**
	 * Adds a new definitionResourceConfig to the provider to read definitions
	 * @param definitionResourceConfig a new resource to read
	 */
	default void addDefinitionResourceConfig(final DefinitionResourceConfig definitionResourceConfig) {
		// nothing by default
	}

}
