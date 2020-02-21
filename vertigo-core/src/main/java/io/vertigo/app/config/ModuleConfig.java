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

import io.vertigo.lang.Assertion;
import io.vertigo.util.ListBuilder;

/**
 * Configuration of a module.
 * This config module contains
 *  - config of components
 *  - config of plugins
 *  - config of resources
 *  - params
 *
 * @author npiedeloup, pchretien
 */
public final class ModuleConfig {
	private final String name;
	private final List<DefinitionProviderConfig> definitionProviders;
	private final List<ComponentConfig> components;
	private final List<PluginConfig> plugins;
	private final List<AspectConfig> aspects;
	private final List<ProxyMethodConfig> proxyMethods;

	ModuleConfig(
			final String name,
			final List<DefinitionProviderConfig> definitionProviderConfigs,
			final List<ComponentConfig> componentConfigs,
			final List<PluginConfig> pluginConfigs,
			final List<AspectConfig> aspectConfigs,
			final List<ProxyMethodConfig> proxyMethods) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(definitionProviderConfigs);
		Assertion.checkNotNull(componentConfigs);
		Assertion.checkNotNull(pluginConfigs);
		Assertion.checkNotNull(aspectConfigs);
		Assertion.checkNotNull(proxyMethods);
		//-----
		this.name = name;
		definitionProviders = Collections.unmodifiableList(new ArrayList<>(definitionProviderConfigs));
		components = Collections.unmodifiableList(new ArrayList<>(componentConfigs));
		plugins = Collections.unmodifiableList(new ArrayList<>(pluginConfigs));
		aspects = aspectConfigs;
		this.proxyMethods = proxyMethods;
	}

	/**
	 * Static method factory for ModuleConfigBuilder
	 * @param name Name of the module
	 * @return ModuleConfigBuilder
	 */
	public static ModuleConfigBuilder builder(final String name) {
		return new ModuleConfigBuilder(name);
	}

	public List<DefinitionProviderConfig> getDefinitionProviderConfigs() {
		return definitionProviders;
	}

	/**
	 * @return the list of the component-configs
	 */
	public List<ComponentConfig> getComponentConfigs() {
		return new ListBuilder<ComponentConfig>()
				.addAll(components)
				.addAll(ConfigUtil.buildConfigs(plugins))
				.build();
	}

	/**
	 * @return the list of the aspect-configs
	 */
	public List<AspectConfig> getAspectConfigs() {
		return aspects;
	}

	/**
	 * @return the list of the proxy configs
	 */
	public List<ProxyMethodConfig> getProxyMethodConfigs() {
		return proxyMethods;
	}

	/**
	 * @return Nom du module.
	 */
	public String getName() {
		return name;
	}

	@Override
	/** {@inheritDoc} */
	public String toString() {
		return name;
	}
}
