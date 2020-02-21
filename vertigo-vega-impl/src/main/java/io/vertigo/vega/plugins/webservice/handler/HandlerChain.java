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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vertigo.lang.Assertion;
import io.vertigo.vega.impl.webservice.WebServiceHandlerPlugin;
import io.vertigo.vega.webservice.exception.SessionException;
import spark.Request;
import spark.Response;

/**
 * Chain of handlers to handle a Request.
 * @author npiedeloup
 */
public final class HandlerChain {
	private static final int MAX_NB_HANDLERS = 50;
	private final List<WebServiceHandlerPlugin> handlers;
	private final int offset;

	/**
	 * Constructor.
	 * @param handlers Handlers
	 */
	public HandlerChain(final List<WebServiceHandlerPlugin> handlers) {
		Assertion.checkNotNull(handlers);
		//-----
		this.handlers = Collections.unmodifiableList(new ArrayList<>(handlers));
		offset = 0;
	}

	/**
	 * private constructor for go forward in chain
	 */
	private HandlerChain(final List<WebServiceHandlerPlugin> handlers, final int offset) {
		Assertion.checkState(offset < MAX_NB_HANDLERS, "HandlerChain go through {0} handlers. Force halt : infinit loop suspected.", MAX_NB_HANDLERS);
		//-----
		this.handlers = handlers;
		this.offset = offset + 1; //new offset
	}

	/**
	 * Do handle of this route.
	 *
	 * @param request spark.Request
	 * @param response spark.Response
	 * @param routeContext Context of this route
	 * @return WebService result
	 * @throws SessionException Session exception
	 */
	public Object handle(final Request request, final Response response, final WebServiceCallContext routeContext) throws SessionException {
		int lookAhead = 0;
		while (offset + lookAhead < handlers.size()) {
			final WebServiceHandlerPlugin nextHandler = handlers.get(offset + lookAhead);
			// >>> before doFilter " + nextHandler
			if (nextHandler.accept(routeContext.getWebServiceDefinition())) {
				return nextHandler.handle(request, response, routeContext, new HandlerChain(handlers, offset + lookAhead));
			}
			//if current  doesn't apply for this WebServiceDefinition we look ahead
			lookAhead++;
			// <<< after doFilter " + nextHandler
		}
		throw new IllegalStateException("Last WebServiceHandlerPlugin haven't send a response body");
	}

}
