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

import java.util.Collection;
import java.util.Optional;

import io.vertigo.core.component.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.vega.impl.webservice.WebServerPlugin;
import io.vertigo.vega.plugins.webservice.handler.HandlerChain;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import spark.Spark;

/**
 * RoutesRegisterPlugin use to register Spark-java route.
 * @author npiedeloup
 */
abstract class AbstractSparkJavaWebServerPlugin implements WebServerPlugin, Activeable {
	private static final String DEFAULT_CONTENT_CHARSET = "UTF-8";
	private final Optional<String> apiPrefix;

	public AbstractSparkJavaWebServerPlugin(final Optional<String> apiPrefix) {
		Assertion.checkNotNull(apiPrefix);
		Assertion.when(apiPrefix.isPresent())
				.check(() -> apiPrefix.get().startsWith("/"), "Global route apiPrefix must starts with /");
		//-----
		this.apiPrefix = apiPrefix;
	}

	@Override
	public void start() {
		// nothing
	}

	@Override
	public void stop() {
		Spark.stop();
		// we need to sleep because spark starts a new thread to stop the server
		try {
			Thread.sleep(100L);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw WrappedException.wrap(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public final void registerWebServiceRoute(final HandlerChain handlerChain, final Collection<WebServiceDefinition> webServiceDefinitions) {
		Assertion.checkNotNull(handlerChain);
		Assertion.checkNotNull(webServiceDefinitions);
		//-----
		boolean corsProtected = false;
		for (final WebServiceDefinition webServiceDefinition : webServiceDefinitions) {
			final String routePath = convertJaxRsPathToSpark(apiPrefix.orElse("") + webServiceDefinition.getPath());
			final String acceptType = webServiceDefinition.getAcceptType();
			final SparkJavaRoute sparkJavaRoute = new SparkJavaRoute(webServiceDefinition, handlerChain, DEFAULT_CONTENT_CHARSET);
			switch (webServiceDefinition.getVerb()) {
				case Get:
					Spark.get(routePath, acceptType, sparkJavaRoute);
					break;
				case Post:
					Spark.post(routePath, acceptType, sparkJavaRoute);
					break;
				case Put:
					Spark.put(routePath, acceptType, sparkJavaRoute);
					break;
				case Patch:
					Spark.patch(routePath, acceptType, sparkJavaRoute);
					break;
				case Delete:
					Spark.delete(routePath, acceptType, sparkJavaRoute);
					break;
				default:
					throw new UnsupportedOperationException();
			}
			corsProtected = corsProtected || webServiceDefinition.isCorsProtected();
		}
		if (corsProtected) {
			final SparkJavaOptionsRoute sparkJavaOptionsRoute = new SparkJavaOptionsRoute(handlerChain);
			Spark.options("*", sparkJavaOptionsRoute);
		}
	}

	private static String convertJaxRsPathToSpark(final String path) {
		return path.replaceAll("\\(", "%28")
				.replaceAll("\\)", "%29")
				.replaceAll("\\{(.+?)\\}", ":$1"); //.+? : Reluctant regexp
	}
}
