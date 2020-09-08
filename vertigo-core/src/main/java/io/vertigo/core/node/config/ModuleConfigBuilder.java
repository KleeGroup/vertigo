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
import io.vertigo.core.node.component.Amplifier;
import io.vertigo.core.node.component.Component;
import io.vertigo.core.node.component.Connector;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.node.component.amplifier.ProxyMethod;
import io.vertigo.core.node.component.aop.Aspect;
import io.vertigo.core.node.definition.DefinitionProvider;
import io.vertigo.core.param.Param;

/**
 * The moduleConfigBuilder defines the configuration of a module.
 * A module has a name.
 * A module is composed of
 *  - components & plugins
 *  - aspects
 *  - definitions (defined by resources or providers)
 *
 * @author npiedeloup, pchretien
 */
public final class ModuleConfigBuilder implements Builder<ModuleConfig> {
	private final String myName;

	private final List<ComponentConfig> myComponentConfigs = new ArrayList<>();
	private final List<AmplifierConfig> myAmplifierConfigs = new ArrayList<>();
	private final List<PluginConfig> myPluginConfigs = new ArrayList<>();
	private final List<ConnectorConfig> myConnectorConfigs = new ArrayList<>();
	private final List<AspectConfig> myAspectConfigs = new ArrayList<>();
	private final List<ProxyMethodConfig> myProxyMethodConfigs = new ArrayList<>();
	private final List<DefinitionProviderConfig> myDefinitionProviderConfigs = new ArrayList<>();

	/**
	 * Constructor.
	 * @param name Name of the module
	 */
	ModuleConfigBuilder(final String name) {
		Assertion.check()
				.isFalse("boot".equalsIgnoreCase(name), "boot is a reserved name")
				.isNotBlank(name);
		//-----
		myName = name;
	}

	/**
	 * Adds an aspect.
	 * @param implClass Class of the aspect
	 * @return this builder
	 */
	public ModuleConfigBuilder addAspect(final Class<? extends Aspect> implClass) {
		myAspectConfigs.add(new AspectConfig(implClass));
		return this;
	}

	/**
	 * Adds a proxy method.
	 * @param proxyMethodClass the proxy method class
	 * @return this builder
	 */
	public ModuleConfigBuilder addProxyMethod(final Class<? extends ProxyMethod> proxyMethodClass) {
		myProxyMethodConfigs.add(new ProxyMethodConfig(proxyMethodClass));
		return this;
	}

	/**
	 * Adds a provider of definitions.
	 * @param definitionProviderConfig the definitionProviderConfig
	 * @return this builder
	 */
	public ModuleConfigBuilder addDefinitionProvider(final DefinitionProviderConfig definitionProviderConfig) {
		Assertion.check()
				.isNotNull(definitionProviderConfig);
		//-----
		myDefinitionProviderConfigs.add(definitionProviderConfig);
		return this;
	}

	/**
	 * Adds a simple provider of definitions.
	 * @param definitionProviderClass the class of the provider
	 * @param params the list of params
	 * @return this builder
	 */
	public ModuleConfigBuilder addDefinitionProvider(final Class<? extends DefinitionProvider> definitionProviderClass, final Param... params) {
		Assertion.check()
				.isNotNull(definitionProviderClass)
				.isNotNull(params);
		//-----
		myDefinitionProviderConfigs.add(
				DefinitionProviderConfig.builder(definitionProviderClass)
						.addAllParams(params)
						.build());
		return this;
	}

	/**
	 * Adds aa amplifier defined by an interface.
	 * @param apiClass api of the amplifie 
	 * @param params the list of params
	 * @return this builder
	 */
	public ModuleConfigBuilder addAmplifier(final Class<? extends Amplifier> apiClass, final Param... params) {
		Assertion.check()
				.isNotNull(apiClass)
				.isNotNull(params);
		//---
		final AmplifierConfig amplifierConfig = new AmplifierConfig(apiClass, Arrays.asList(params));
		myAmplifierConfigs.add(amplifierConfig);
		return this;
	}

	/**
	* Adds a component defined by an implementation.
	 * @param implClass impl of the component
	 * @param params the list of params
	 * @return this builder
	 */
	public ModuleConfigBuilder addComponent(final Class<? extends Component> implClass, final Param... params) {
		Assertion.check()
				.isNotNull(implClass)
				.isNotNull(params);
		//---
		final ComponentConfig componentConfig = ComponentConfig.of(implClass, params);
		return addComponent(componentConfig);
	}

	/**
	* Adds a component defined by an api and an implementation.
	 * @param apiClass api of the component
	 * @param implClass impl of the component
	 * @param params the list of params
	 * @return this builder
	 */
	public ModuleConfigBuilder addComponent(final Class<? extends Component> apiClass, final Class<? extends Component> implClass, final Param... params) {
		Assertion.check()
				.isNotNull(apiClass)
				.isNotNull(implClass)
				.isNotNull(params);
		//---
		final ComponentConfig componentConfig = ComponentConfig.of(apiClass, implClass, params);
		return addComponent(componentConfig);
	}

	/**
	* Adds a component defined by its config.
	 * @param componentConfig the config of the component
	 * @return this builder
	 */
	public ModuleConfigBuilder addComponent(final ComponentConfig componentConfig) {
		Assertion.check()
				.isNotNull(componentConfig);
		//---
		myComponentConfigs.add(componentConfig);
		return this;
	}

	/**
	 * Adds a plugin defined by its implementation.
	 * @param pluginImplClass  impl of the plugin
	 * @param params  the list of params
	 * @return this builder
	 */
	public ModuleConfigBuilder addPlugin(final Class<? extends Plugin> pluginImplClass, final Param... params) {
		return addPlugin(pluginImplClass, Arrays.asList(params));
	}

	public ModuleConfigBuilder addPlugin(final Class<? extends Plugin> pluginImplClass, final List<Param> params) {
		return addPlugin(ConfigUtil.getPluginApi(pluginImplClass), pluginImplClass, params);
	}

	public ModuleConfigBuilder addPlugin(final Class<? extends Plugin> pluginApiClass, final Class<? extends Plugin> pluginImplClass, final List<Param> params) {
		myPluginConfigs.add(new PluginConfig(pluginApiClass, pluginImplClass, params));
		return this;
	}

	/**
	 * Adds a connector defined by its config.
	 * @param connectorConfig the connector-config
	 * @return this builder
	 */

	/**
	 * Adds a connector defined by its implementation.
	 * @param connectorImplClass  impl of the connector
	 * @param params  the list of params
	 * @return this builder
	 */
	public ModuleConfigBuilder addConnector(final Class<? extends Connector> connectorImplClass, final Param... params) {
		myConnectorConfigs.add(
				new ConnectorConfig(
						ConfigUtil.getConnectorApiOpt(connectorImplClass),
						connectorImplClass,
						Arrays.asList(params)));
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ModuleConfig build() {
		return new ModuleConfig(
				myName,
				myDefinitionProviderConfigs,
				myComponentConfigs,
				myPluginConfigs,
				myConnectorConfigs,
				myAmplifierConfigs,
				myAspectConfigs,
				myProxyMethodConfigs);
	}

}
