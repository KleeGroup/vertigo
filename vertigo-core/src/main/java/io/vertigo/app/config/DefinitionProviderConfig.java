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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertigo.core.definition.DefinitionProvider;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;

/**
 * @author mlaroche
 */
public final class DefinitionProviderConfig {
	private final Class<? extends DefinitionProvider> definitionProviderClass;
	private final Map<String, String> params;
	private final List<DefinitionResourceConfig> definitionResources;

	DefinitionProviderConfig(
			final Class<? extends DefinitionProvider> definitionProviderClass,
			final List<Param> params,
			final List<DefinitionResourceConfig> definitionResourceConfigs) {
		Assertion.checkNotNull(definitionProviderClass);
		Assertion.checkNotNull(params);
		Assertion.checkNotNull(definitionResourceConfigs);
		//-----
		this.definitionProviderClass = definitionProviderClass;
		this.params = params
				.stream()
				.collect(Collectors.toMap(Param::getName, Param::getValue));
		definitionResources = Collections.unmodifiableList(new ArrayList<>(definitionResourceConfigs));
	}

	/**
	 * Static method factory for DefinitionProviderConfigBuilder
	 * @param definitionProviderClass the class of the definitionProvider
	 * @return ComponentConfigBuilder
	 */
	public static DefinitionProviderConfigBuilder builder(final Class<? extends DefinitionProvider> definitionProviderClass) {
		return new DefinitionProviderConfigBuilder(definitionProviderClass);
	}

	public Class<? extends DefinitionProvider> getDefinitionProviderClass() {
		return definitionProviderClass;
	}

	public List<DefinitionResourceConfig> getDefinitionResourceConfigs() {
		return definitionResources;
	}

	/**
	 * @return the params
	 */
	public Map<String, String> getParams() {
		return params;
	}

	@Override
	public String toString() {
		return "{ className: " + definitionProviderClass.getName() + " }";
	}
}
