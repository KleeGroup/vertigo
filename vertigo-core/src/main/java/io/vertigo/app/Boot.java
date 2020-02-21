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
package io.vertigo.app;

import java.io.File;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import io.vertigo.app.config.BootConfig;
import io.vertigo.app.config.LogConfig;
import io.vertigo.lang.Assertion;

/**
 * The Boot class is reponsible for the boot phase.
 *
 * @author pchretien
 */
final class Boot {
	private final BootConfig bootConfig;

	/**
	 * Constructor.
	 * @param bootConfig The config of the boot
	 */
	Boot(final BootConfig bootConfig) {
		Assertion.checkNotNull(bootConfig);
		//-----
		this.bootConfig = bootConfig;
	}

	private static void initLog(final LogConfig log4Config) {
		Assertion.checkNotNull(log4Config);
		//-----
		final String log4jFileName = log4Config.getFileName();
		Assertion.checkArgument(log4jFileName.endsWith(".xml"), "Use the XML format for log4j configurations (instead of : {0}).", log4jFileName);
		final URL url = Home.class.getResource(log4jFileName);
		if (url != null) {
			Configurator.initialize("definedLog4jContext", Home.class.getClassLoader(), log4jFileName);
			LogManager.getRootLogger().info("Log4J configuration chargée (resource) : {}", url.getFile());
		} else {
			Assertion.checkArgument(new File(log4jFileName).exists(), "Fichier de configuration log4j : {0} est introuvable", log4jFileName);
			// Avec configureAndWatch (utilise un anonymous thread)
			// on peut modifier à chaud le fichier de conf log4j
			// mais en cas de hot-deploy, le thread reste présent ce qui peut-entrainer des problèmes.
			Configurator.initialize("definedLog4jContext", null, log4jFileName);
			LogManager.getRootLogger().info("Log4J configuration chargée (fichier) : {}", log4jFileName);
		}
	}

	void init() {
		bootConfig.getLogConfig().ifPresent(Boot::initLog);
	}
}
