/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.definition;

import java.util.List;

import io.vertigo.core.node.config.DefinitionResourceConfig;

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
