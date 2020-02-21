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
package io.vertigo.vega.plugins.webservice.webserver.sparkjava;

import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.core.param.ParamValue;
import io.vertigo.vega.impl.webservice.filter.JettyMultipartCleaner;
import io.vertigo.vega.impl.webservice.filter.JettyMultipartConfig;
import spark.Spark;

/**
 * RoutesRegisterPlugin use to register Spark-java route.
 * @author npiedeloup
 */
public final class SparkJavaEmbeddedWebServerPlugin extends AbstractSparkJavaWebServerPlugin {

	/**
	 * @param apiPrefix globale api prefix
	 * @param port Server port
	 */
	@Inject
	public SparkJavaEmbeddedWebServerPlugin(
			@ParamValue("apiPrefix") final Optional<String> apiPrefix,
			@ParamValue("port") final int port) {
		super(apiPrefix);
		Spark.port(port);
		//---
		final String tempDir = System.getProperty("java.io.tmpdir");
		Spark.before(new JettyMultipartConfig(tempDir));
		Spark.after(new JettyMultipartCleaner());
	}
}
