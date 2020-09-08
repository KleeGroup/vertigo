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
package io.vertigo.core.node.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;
import io.vertigo.core.node.definition.DefinitionProvider;
import io.vertigo.core.param.Param;

/**
 * The DefinitionProviderConfigBuilder defines the configuration of a definitionProvider.
 * A definition is composed of
 *  - a class that provides definitions
 *  - parameters
 *  - resources
 *
 * @author mlaroche
 */
public final class DefinitionProviderConfigBuilder implements Builder<DefinitionProviderConfig> {
	private final Class<? extends DefinitionProvider> myClass;

	private final List<DefinitionResourceConfig> myDefinitionResourceConfigs = new ArrayList<>();
	private final List<Param> myParams = new ArrayList<>();

	/**
	 * Constructor.
	 * @param definitionProviderClass the class of the definitionProvider
	 */

	DefinitionProviderConfigBuilder(final Class<? extends DefinitionProvider> definitionProviderClass) {
		Assertion.check()
				.isNotNull(definitionProviderClass);
		//-----
		myClass = definitionProviderClass;
	}

	/**
	 * Adds definitions defined by a resource file.
	 * @param resourceType Type of the resource
	 * @param resourcePath Path of the resource
	* @return this builder
	 */
	public DefinitionProviderConfigBuilder addDefinitionResource(final String resourceType, final String resourcePath) {
		Assertion.check()
				.isNotBlank(resourceType)
				.isNotNull(resourcePath);
		//-----
		myDefinitionResourceConfigs.add(new DefinitionResourceConfig(resourceType, resourcePath));
		return this;
	}

	/**
	 * Adds a param to this definitionProvider.
	 * @param params the list of params
	 * @return this builder
	 */
	public DefinitionProviderConfigBuilder addAllParams(final Param... params) {
		Assertion.check()
				.isNotNull(params);
		//-----
		myParams.addAll(Arrays.asList(params));
		return this;
	}

	/**
	 * Adds a param to this definitionProvider.
	 * @param param the param
	 * @return this builder
	 */
	public DefinitionProviderConfigBuilder addParam(final Param param) {
		Assertion.check()
				.isNotNull(param);
		//-----
		myParams.add(param);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public DefinitionProviderConfig build() {
		return new DefinitionProviderConfig(
				myClass,
				myParams,
				myDefinitionResourceConfigs);
	}

}
