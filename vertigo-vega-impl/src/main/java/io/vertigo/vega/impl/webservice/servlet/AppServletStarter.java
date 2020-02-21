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
package io.vertigo.vega.impl.webservice.servlet;

import java.util.Properties;

import io.vertigo.app.config.LogConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.yaml.YamlAppConfigBuilder;

/**
 * @author npiedeloup
 */
final class AppServletStarter extends AbstractAppServletStarter {

	/** {@inheritDoc} */
	@Override
	NodeConfig buildNodeConfig(final Properties bootConf) {
		final YamlAppConfigBuilder nodeConfigBuilder = new YamlAppConfigBuilder(bootConf);
		nodeConfigBuilder.beginBoot();

		//si présent on récupère le paramétrage du fichier externe de paramétrage log4j
		if (bootConf.containsKey(LOG4J_CONFIGURATION_PARAM_NAME)) {
			final String logFileName = bootConf.getProperty(LOG4J_CONFIGURATION_PARAM_NAME);
			bootConf.remove(LOG4J_CONFIGURATION_PARAM_NAME);
			//-----
			nodeConfigBuilder.withLogConfig(new LogConfig(logFileName));
		}

		final String configFileNames = bootConf.getProperty("boot.applicationConfiguration");
		final String[] configFileNamesSplit = configFileNames.split(";");
		bootConf.remove("boot.applicationConfiguration");
		//-----
		nodeConfigBuilder.withFiles(getClass(), configFileNamesSplit);

		// Initialisation de l'état de l'application
		return nodeConfigBuilder.build();
	}
}
