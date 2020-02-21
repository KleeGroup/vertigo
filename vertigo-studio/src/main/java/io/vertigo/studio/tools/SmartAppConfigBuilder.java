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
package io.vertigo.studio.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.xml.XmlAppConfigBuilder;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.WrappedException;

/**
 * Génération des fichiers Java et SQL à patrir de fichiers template freemarker.
 *
 * @author dchallas, pchretien
 */
public final class SmartAppConfigBuilder implements Builder<NodeConfig> {
	private final NodeConfig nodeConfig;

	public SmartAppConfigBuilder(final String[] args) {
		//---
		this(loadProperties(args, SmartAppConfigBuilder.class));
	}

	private SmartAppConfigBuilder(final Properties conf) {
		Assertion.checkNotNull(conf);
		//---
		nodeConfig = buildNodeConfig(conf);
	}

	private static NodeConfig buildNodeConfig(final Properties conf) {
		// Initialisation de l'état de l'application
		final XmlAppConfigBuilder nodeConfigBuilder = new XmlAppConfigBuilder();
		if (conf.containsKey("boot.applicationConfiguration")) {
			final String xmlModulesFileNames = conf.getProperty("boot.applicationConfiguration");
			final String[] xmlFileNamesSplit = xmlModulesFileNames.split(";");
			conf.remove("boot.applicationConfiguration");
			//-----
			nodeConfigBuilder.withModules(SmartAppConfigBuilder.class, conf, xmlFileNamesSplit);
		}

		return nodeConfigBuilder
				.build();
	}

	@Override
	public NodeConfig build() {
		return nodeConfig;
	}

	private static Properties loadProperties(final String[] args, final Class<?> relativeRootClass) {
		Assertion.checkArgument(args.length == 1, "Usage : java io.vertigo.studio.tools.NameSpace2Java \"<<pathToParams.properties>>\" ");
		//---
		return loadProperties(args[0], relativeRootClass);
	}

	private static Properties loadProperties(final String propertiesName, final Class<?> relativeRootClass) {
		final URL url = relativeRootClass.getResource(propertiesName);
		Assertion.checkNotNull(url, "Unable to find file :{0} in classRoot {1}", propertiesName, relativeRootClass);
		//-----
		try (final InputStream in = url.openStream()) {
			final Properties properties = new Properties();
			properties.load(in);
			return properties;
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

}
