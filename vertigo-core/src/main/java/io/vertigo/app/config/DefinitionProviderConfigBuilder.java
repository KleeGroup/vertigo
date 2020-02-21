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
package io.vertigo.app.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.vertigo.core.definition.DefinitionProvider;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

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
		Assertion.checkNotNull(definitionProviderClass);
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
		Assertion.checkArgNotEmpty(resourceType);
		Assertion.checkNotNull(resourcePath);
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
		Assertion.checkNotNull(params);
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
		Assertion.checkNotNull(param);
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
