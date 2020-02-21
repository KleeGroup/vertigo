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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.util.ClassUtil;
import io.vertigo.vega.plugins.webservice.handler.HandlerChain;
import io.vertigo.vega.plugins.webservice.handler.WebServiceCallContext;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition.Verb;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handler of Options preflight request.
 * @author npiedeloup
 */
public final class SparkJavaOptionsRoute implements Route {
	private static final Logger LOGGER = LogManager.getLogger(SparkJavaOptionsRoute.class);

	private final HandlerChain handlerChain;
	private final WebServiceDefinition webServiceCors;

	/**
	 * @param handlerChain handlerChain
	 */
	SparkJavaOptionsRoute(final HandlerChain handlerChain) {
		this.handlerChain = handlerChain;
		//we use a fake webServiceDefinition, to ensure no webservice was called on Options request
		webServiceCors = WebServiceDefinition.builder(ClassUtil.findMethod(SparkJavaOptionsRoute.class, "unsupported"))
				.with(Verb.Get, "/_OPTIONS_*")
				.withCorsProtected(true)
				.build();
	}

	/**
	 * Fake method for prefligth Options request.
	 */
	public void unsupported() {
		throw new UnsupportedOperationException("OPTIONS is unsupported but preflight");
	}

	/** {@inheritDoc} */
	@Override
	public Object handle(final Request request, final Response response) {
		try {
			return handlerChain.handle(request, response, new WebServiceCallContext(request, response, webServiceCors)); //no WebService
		} catch (final Exception e) {
			LOGGER.error("Option route error", e);
			return e.getMessage();
		}
	}
}
