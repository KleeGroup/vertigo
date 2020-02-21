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
package io.vertigo.vega.impl.webservice;

import io.vertigo.core.component.Plugin;
import io.vertigo.vega.plugins.webservice.handler.HandlerChain;
import io.vertigo.vega.plugins.webservice.handler.WebServiceCallContext;
import io.vertigo.vega.webservice.exception.SessionException;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import spark.Request;
import spark.Response;

/**
 * Handler of WebService Route, are defined as plugins of WebServiceManager.
 * @author npiedeloup
 */
public interface WebServiceHandlerPlugin extends Plugin {

	/**
	 * @param webServiceDefinition WebServiceDefinition
	 * @return If this handler should be use for this webService
	 */
	boolean accept(WebServiceDefinition webServiceDefinition);

	/**
	 * Do handle of this route.
	 *
	 * @param request spark.Request
	 * @param response spark.Response
	 * @param webServiceCallContext Context of this request
	 * @param chain current HandlerChain.
	 * @return Response body
	 * @throws SessionException Session expired exception
	 */
	Object handle(
			final Request request,
			final Response response,
			final WebServiceCallContext webServiceCallContext,
			final HandlerChain chain) throws SessionException;

}
