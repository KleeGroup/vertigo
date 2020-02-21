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
package io.vertigo.vega.plugins.webservice.handler;

import javax.inject.Inject;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.vega.impl.webservice.WebServiceHandlerPlugin;
import io.vertigo.vega.webservice.exception.SessionException;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import spark.Request;
import spark.Response;

/**
 * Analytics handler.
 * @author npiedeloup
 */
public final class AnalyticsWebServiceHandlerPlugin implements WebServiceHandlerPlugin {

	private final AnalyticsManager analyticsManager;

	/**
	 * Constructor.
	 * @param analyticsManager Analytics Manager
	 */
	@Inject
	public AnalyticsWebServiceHandlerPlugin(final AnalyticsManager analyticsManager) {
		Assertion.checkNotNull(analyticsManager);
		//-----
		this.analyticsManager = analyticsManager;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final WebServiceDefinition webServiceDefinition) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Object handle(final Request request, final Response response, final WebServiceCallContext webServiceCallContext, final HandlerChain chain) throws SessionException {
		final WebServiceDefinition webServiceDefinition = webServiceCallContext.getWebServiceDefinition();
		//On ne prend pas request.pathInfo qui peut contenir des paramètres : on en veut pas ca dans les stats
		final String name = "/" + webServiceDefinition.getVerb().name() + "/" + webServiceDefinition.getPath();
		return analyticsManager.traceWithReturn(
				"webservices",
				name,
				tracer -> {
					try {
						return chain.handle(request, response, webServiceCallContext);
					} catch (final SessionException e) {
						throw WrappedException.wrap(e);
					}
				});
	}
}
