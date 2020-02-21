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

import java.util.Arrays;
import java.util.List;

import io.vertigo.core.component.Plugin;
import io.vertigo.core.component.di.DIAnnotationUtil;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.StringUtil;

/**
 * This class defines the configuration of a plugin.
 *
 * A plugin is defined by
 *  - a implemenation class
 *  - a map of params
 *  - a type
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
	private final List<Param> params;
	private final String pluginType;

	/**
	 * Constructor.
	 * @param implClass the impl class of the component
	 * @param params the params
	 */
	public PluginConfig(final Class<? extends Plugin> implClass, final List<Param> params) {
		Assertion.checkNotNull(implClass);
		Assertion.checkArgument(Plugin.class.isAssignableFrom(implClass), "impl class {0} must implement {1}", implClass, Plugin.class);
		Assertion.checkNotNull(params);
		//-----
		this.implClass = implClass;

		this.params = params;
		pluginType = StringUtil.first2LowerCase(getType(implClass));
	}

	/**
	 * Static method factory for PluginConfigBuilder
	 * @param pluginImplClass impl of the plugin
	 * @return PluginConfigBuilder
	 */
	public static PluginConfigBuilder builder(final Class<? extends Plugin> pluginImplClass) {
		return new PluginConfigBuilder(pluginImplClass);
	}

	/**
	 * @return the type of the plugin
	 */
	public String getPluginType() {
		return pluginType;
	}

	/**
	 * @return the impl class of the component
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

	@Override
	/** {@inheritDoc} */
	public String toString() {
		return implClass.getName();
	}

	/*
	 * We are looking for the type of the plugin.
	 * This type is the first objector interface that inherits from then 'plugin' interface.
	 */
	private static String getType(final Class<? extends Plugin> pluginImplClass) {
		//We are seeking the first and unique Object that extends Plugin.
		//This Interface defines the type of the plugin.

		for (final Class intf : ClassUtil.getAllInterfaces(pluginImplClass)) {
			if (Arrays.asList(intf.getInterfaces()).contains(Plugin.class)) {
				return DIAnnotationUtil.buildId(intf);
			}
		}
		//We have found nothing among the interfaces.
		//we are drilling the classes to look for a class that inherits the plugin.
		for (Class currentClass = pluginImplClass; currentClass != null; currentClass = currentClass.getSuperclass()) {
			if (Arrays.asList(currentClass.getInterfaces()).contains(Plugin.class)) {
				return DIAnnotationUtil.buildId(currentClass);
			}
		}
		throw new IllegalArgumentException("A plugin must extends an interface|class that defines its contract : " + pluginImplClass);
	}
}
