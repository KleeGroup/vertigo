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

import io.vertigo.core.component.Plugin;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * The pluginConfigBuilder defines the configuration of a plugin.
 * A plugin is a way to parameterize a component.
  *
 * @author npiedeloup, pchretien
 */
public final class PluginConfigBuilder implements Builder<PluginConfig> {
	private final Class<? extends Plugin> myPluginImplClass;
	private final List<Param> myParams = new ArrayList<>();

	/**
	 * Constructor.
	 * @param pluginImplClass impl of the plugin
	 */
	PluginConfigBuilder(final Class<? extends Plugin> pluginImplClass) {
		Assertion.checkNotNull(pluginImplClass);
		//-----
		myPluginImplClass = pluginImplClass;
	}

	/**
	 * Adds a param to this plugin.
	 * @param params the list of params
	 * @return this builder
	 */
	public PluginConfigBuilder addAllParams(final Param... params) {
		Assertion.checkNotNull(params);
		//-----
		myParams.addAll(Arrays.asList(params));
		return this;
	}

	/**
	 * Adds a param to this plugin.
	 * @param param the param
	 * @return this builder
	 */
	public PluginConfigBuilder addParam(final Param param) {
		Assertion.checkNotNull(param);
		//-----
		myParams.add(param);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public PluginConfig build() {
		return new PluginConfig(
				myPluginImplClass,
				myParams);
	}
}
