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
package io.vertigo.core.plugins.param.properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

import javax.inject.Inject;

import io.vertigo.core.impl.param.ParamPlugin;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.Param;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.resource.ResourceManager;

/**
 * Plugin de gestion de configuration de fichiers properties.
 *
 * @author skerdudou
 */
public final class PropertiesParamPlugin implements ParamPlugin {
	private final Properties properties;

	/**
	 * Constructor.
	 *
	 * @param resourceManager Selector
	 * @param url Url du fichier XML de configuration
	 * @throws IOException erreur de lecture du fichier
	 */
	@Inject
	public PropertiesParamPlugin(final ResourceManager resourceManager, @ParamValue("url") final String url) throws IOException {
		Assertion.check()
				.isNotNull(resourceManager)
				.isNotBlank(url);
		//-----
		final URL configURL = resourceManager.resolve(url);
		properties = loadProperties(configURL);
	}

	private static Properties loadProperties(final URL configURL) throws IOException {
		try (final InputStream input = configURL.openStream()) {
			final Properties tmpProperties = new Properties();
			tmpProperties.load(input);
			return tmpProperties;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Param> getParam(final String paramName) {
		Assertion.check().isNotBlank(paramName);
		//-----
		final String paramValue = properties.getProperty(paramName);
		return paramValue != null ? Optional.of(Param.of(paramName, paramValue)) : Optional.empty();
	}
}
