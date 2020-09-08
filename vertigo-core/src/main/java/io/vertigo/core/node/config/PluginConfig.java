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

import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.param.Param;

/**
 * This class defines the configuration of a plugin.
 *
 * A plugin is defined by
 *  - an api (an interface that extends Plugin ) 
 *  - an implemenation class of the api
 *  - a map of params
 *
 * Several plugins may have the same type.
 *  - for example : a metaDataExtractor plugin has many implementations to deal with all the formats
 * The same plugin can be used many times with distincts params
 *  - for example : a plugin to listen on a specific channel will be used with many params (as many as channels)
 *
 * @author npiedeloup, pchretien
 */
public final class PluginConfig {
	private final Class<? extends Plugin> implClass;
	private final Class<? extends Plugin> apiClass;
	private final List<Param> params;

	/**
	 * Constructor.
	 * @param apiClass api of the plugin
	 * @param implClass the impl class of the plugin
	 * @param params the params
	 */
	PluginConfig(final Class<? extends Plugin> apiClass, final Class<? extends Plugin> implClass, final List<Param> params) {
		Assertion.check()
				.isNotNull(apiClass)
				.isNotNull(implClass)
				.isTrue(Plugin.class.isAssignableFrom(apiClass), "api class {0} must implement {1}", apiClass, Plugin.class)
				.isTrue(apiClass.isAssignableFrom(implClass), "impl class {0} must implement {1}", implClass, apiClass)
				.isTrue(apiClass.isInterface(), "api class {0} must be an interface", apiClass)
				.isNotNull(params);
		//-----
		this.apiClass = apiClass;
		this.implClass = implClass;
		this.params = params;
	}

	/**
	 * @return the api class
	 */
	public Class<? extends Plugin> getApiClass() {
		return apiClass;
	}

	/**
	 * @return the impl class
	 */
	public Class<? extends Plugin> getImplClass() {
		return implClass;
	}

	/**
	 * @return the params
	 */
	public List<Param> getParams() {
		return params;
	}
}
