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

import io.vertigo.core.lang.Builder;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.param.Param;

/**
 * Defines a module by its features.
 * @author pchretien
 */
public abstract class Features<F> implements Builder<ModuleConfig> {
	private final ModuleConfigBuilder moduleConfigBuilder;

	protected Features(final String name) {
		moduleConfigBuilder = ModuleConfig.builder(name);
	}

	protected abstract void buildFeatures();

	protected final ModuleConfigBuilder getModuleConfigBuilder() {
		return moduleConfigBuilder;
	}

	public final F addPlugin(final Class<? extends Plugin> pluginImplClass, final Param... params) {
		moduleConfigBuilder.addPlugin(pluginImplClass, params);
		return (F) this;
	}

	@Override
	public final ModuleConfig build() {
		buildFeatures();
		return moduleConfigBuilder.build();
	}

}
