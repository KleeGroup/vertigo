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

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import io.vertigo.account.authorization.VSecurityException;
import io.vertigo.core.locale.MessageText;
import io.vertigo.core.param.ParamValue;
import io.vertigo.vega.impl.webservice.WebServiceHandlerPlugin;
import io.vertigo.vega.webservice.exception.SessionException;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import spark.Request;
import spark.Response;

/**
 * Handler of Cross-Origin Resource Sharing (CORS).
 * @see "https://www.owasp.org/index.php/CORS_OriginHeaderScrutiny"
 * @author npiedeloup
 */
public final class CorsAllowerWebServiceHandlerPlugin implements WebServiceHandlerPlugin {

	private static final String REQUEST_HEADER_ORIGIN = "Origin";

	private static final String DEFAULT_ALLOW_ORIGIN_CORS_FILTER = "*";
	private static final String DEFAULT_ALLOW_METHODS_CORS_FILTER = "GET, POST, DELETE, PUT, OPTIONS"; // may use *
	private static final String DEFAULT_ALLOW_HEADERS_CORS_FILTER = "Content-Type, Cache-Control, X-Requested-With"; // may use *
	private static final String DEFAULT_EXPOSED_HEADERS_CORS_FILTER = "Content-Type, listServerToken, content-length, x-total-count, x-access-token"; // may use *

	private final String originCORSFilter;
	private final String methodCORSFilter;
	private final Set<String> originCORSFiltersSet;
	private final Set<String> methodCORSFiltersSet;

	/**
	 * @param originCORSFilter Origin CORS Allowed
	 * @param methodCORSFilter Method CORS Allowed
	 */
	@Inject
	public CorsAllowerWebServiceHandlerPlugin(
			@ParamValue("originCORSFilter") final Optional<String> originCORSFilter,
			@ParamValue("methodCORSFilter") final Optional<String> methodCORSFilter) {
		this.originCORSFilter = originCORSFilter.orElse(DEFAULT_ALLOW_ORIGIN_CORS_FILTER);
		this.methodCORSFilter = methodCORSFilter.orElse(DEFAULT_ALLOW_METHODS_CORS_FILTER);
		originCORSFiltersSet = parseStringToSet(this.originCORSFilter);
		methodCORSFiltersSet = parseStringToSet(this.methodCORSFilter);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final WebServiceDefinition webServiceDefinition) {
		return webServiceDefinition.isCorsProtected();
	}

	/** {@inheritDoc} */
	@Override
	public Object handle(final Request request, final Response response, final WebServiceCallContext routeContext, final HandlerChain chain) throws SessionException {
		putCorsResponseHeaders(request, response);
		if ("OPTIONS".equalsIgnoreCase(request.raw().getMethod())) { //if Options request, we stop here
			response.status(HttpServletResponse.SC_OK);
			response.type("application/json;charset=UTF-8");
			return "";
		}
		return chain.handle(request, response, routeContext);
	}

	/**
	 * @param request Request
	 * @param response Response
	 */
	public void putCorsResponseHeaders(final Request request, final Response response) {
		/** @see "https://www.owasp.org/index.php/CORS_OriginHeaderScrutiny" */
		/* Step 1 : Check that we have only one and non empty instance of the "Origin" header */
		final String origin = request.headers(REQUEST_HEADER_ORIGIN);
		if (origin != null) {
			final String method = request.raw().getMethod();
			if (!isAllowed(origin, originCORSFiltersSet) || !isAllowed(method, methodCORSFiltersSet)) {
				response.status(HttpServletResponse.SC_FORBIDDEN);
				response.raw().resetBuffer();
				throw new VSecurityException(MessageText.of("Invalid CORS Access (Origin:{0}, Method:{1})", origin, method));
			}
		}
		response.header("Access-Control-Allow-Origin", originCORSFilter);
		response.header("Access-Control-Allow-Methods", methodCORSFilter);
		response.header("Access-Control-Allow-Headers", DEFAULT_ALLOW_HEADERS_CORS_FILTER);
		response.header("Access-Control-Expose-Headers", DEFAULT_EXPOSED_HEADERS_CORS_FILTER);
	}

	private static boolean isAllowed(final String currentValue, final Set<String> allowedValues) {
		if (allowedValues.contains("*")) {
			return true;
		} else if (currentValue.trim().isEmpty()) {
			return false;
		}
		return allowedValues.contains(currentValue);
	}

	private static Set<String> parseStringToSet(final String param) {
		return Arrays.stream(param.split(","))
				.map(String::trim)
				.collect(Collectors.toSet());
	}
}
