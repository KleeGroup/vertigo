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
import java.util.Optional;

import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.daemon.DaemonManager;
import io.vertigo.core.impl.analytics.AnalyticsConnectorPlugin;
import io.vertigo.core.impl.analytics.AnalyticsManagerImpl;
import io.vertigo.core.impl.daemon.DaemonManagerImpl;
import io.vertigo.core.impl.locale.LocaleManagerImpl;
import io.vertigo.core.impl.param.ParamManagerImpl;
import io.vertigo.core.impl.resource.ResourceManagerImpl;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.core.node.component.AopPlugin;
import io.vertigo.core.node.component.Component;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.param.Param;
import io.vertigo.core.param.ParamManager;
import io.vertigo.core.plugins.analytics.log.SmartLoggerAnalyticsConnectorPlugin;
import io.vertigo.core.plugins.analytics.log.SocketLoggerAnalyticsConnectorPlugin;
import io.vertigo.core.plugins.component.aop.javassist.JavassistAopPlugin;
import io.vertigo.core.resource.ResourceManager;

/**
 * Configuration.
 *
 * @author npiedeloup, pchretien
 */
public final class BootConfigBuilder implements Builder<BootConfig> {
	private Optional<LogConfig> myLogConfigOpt = Optional.empty(); //par défaut
	private boolean myVerbose;
	private AopPlugin myAopPlugin = new JavassistAopPlugin(); //By default
	private final List<ComponentConfig> myComponentConfigs = new ArrayList<>();
	private final List<PluginConfig> myPluginConfigs = new ArrayList<>();

	/**
	 * @param nodeConfigBuilder Parent NodeConfig builder
	 */
	BootConfigBuilder() {
	}

	/**
	 * Opens the boot module.
	 * There is exactly one BootConfig per NodeConfig.
	 *
	 * @param locales a string which contains all the locales separated with a simple comma : ',' .
	 * @return this builder
	 */
	public BootConfigBuilder withLocales(final String locales) {
		addComponent(
				LocaleManager.class,
				LocaleManagerImpl.class,
				Param.of("locales", locales));
		return this;
	}

	/**
	 * Opens the boot module.
	 * There is exactly one BootConfig per NodeConfig.
	 * With a default ZoneId for DateTime formatter.
	 *
	 * @param locales a string which contains all the locales separated with a simple comma : ',' .
	 * @param defaultZoneId a string which contains defaultZoneId.
	 * @return this builder
	 */
	public BootConfigBuilder withLocalesAndDefaultZoneId(final String locales, final String defaultZoneId) {
		addComponent(
				LocaleManager.class,
				LocaleManagerImpl.class,
				Param.of("locales", locales),
				Param.of("defaultZoneId", defaultZoneId));
		return this;
	}

	/**
	 * Ajout de paramètres
	 * @param logConfig Config of logs
	 * @return this builder
	 */
	public BootConfigBuilder withLogConfig(final LogConfig logConfig) {
		Assertion.check()
				.isNotNull(logConfig);
		//-----
		myLogConfigOpt = Optional.of(logConfig);
		return this;
	}

	/**
	 * Enables verbosity during startup
	 * @return this builder
	 */
	public BootConfigBuilder verbose() {
		myVerbose = true;
		return this;
	}

	/**
	 * @param aopPlugin AopPlugin
	 * @return this builder
	 */
	public BootConfigBuilder withAopEngine(final AopPlugin aopPlugin) {
		Assertion.check()
				.isNotNull(aopPlugin);
		//-----
		myAopPlugin = aopPlugin;
		return this;
	}

	@Feature("analytics.socketLoggerConnector")
	public BootConfigBuilder withSocketLoggerAnalyticsConnector(final Param... params) {
		addPlugin(SocketLoggerAnalyticsConnectorPlugin.class, params);
		return this;

	}

	@Feature("analytics.smartLoggerConnector")
	public BootConfigBuilder withSmartLoggerAnalyticsConnector(final Param... params) {
		addPlugin(SmartLoggerAnalyticsConnectorPlugin.class, params);
		return this;

	}

	/**
	 * Adds a AnalyticsConnectorPlugin
	 * @param analyticsConnectorPluginClass the plugin to use
	 * @param params the params
	 * @return these features
	 */
	public BootConfigBuilder addAnalyticsConnectorPlugin(final Class<? extends AnalyticsConnectorPlugin> analyticsConnectorPluginClass, final Param... params) {
		return addPlugin(analyticsConnectorPluginClass, params);
	}

	/**
	* Adds a component defined by an api and an implementation.
	 * @param apiClass api of the component
	 * @param implClass impl of the component
	 * @param params the list of params
	 * @return this builder
	 */
	private BootConfigBuilder addComponent(final Class<? extends Component> apiClass, final Class<? extends Component> implClass, final Param... params) {
		final ComponentConfig componentConfig = ComponentConfig.of(apiClass, implClass, params);
		myComponentConfigs.add(componentConfig);
		return this;
	}

	/**
	 * Adds a plugin defined by its implementation.
	 * @param pluginImplClass  impl of the plugin
	 * @param params the list of params
	 * @return this builder
	 */
	public BootConfigBuilder addPlugin(final Class<? extends Plugin> pluginImplClass, final Param... params) {
		return addPlugin(new PluginConfig(ConfigUtil.getPluginApi(pluginImplClass), pluginImplClass, Arrays.asList(params)));
	}

	/**
	 * Adds a plugin defined by its builder.
	 * @param pluginConfig the plugin-config
	 * @return this builder
	 */
	public BootConfigBuilder addPlugin(final PluginConfig pluginConfig) {
		Assertion.check()
				.isNotNull(pluginConfig);
		//---
		myPluginConfigs.add(pluginConfig);
		return this;
	}

	/**
	 * @return BootConfig
	 */
	@Override
	public BootConfig build() {
		addComponent(ResourceManager.class, ResourceManagerImpl.class)
				.addComponent(ParamManager.class, ParamManagerImpl.class)
				.addComponent(DaemonManager.class, DaemonManagerImpl.class)
				.addComponent(AnalyticsManager.class, AnalyticsManagerImpl.class);

		return new BootConfig(
				myLogConfigOpt,
				myComponentConfigs,
				myPluginConfigs,
				myAopPlugin,
				myVerbose);
	}

}
