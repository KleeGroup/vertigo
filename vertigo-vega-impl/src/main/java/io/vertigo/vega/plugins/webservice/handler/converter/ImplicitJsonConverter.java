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
package io.vertigo.vega.plugins.webservice.handler.converter;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.vertigo.lang.Assertion;
import io.vertigo.vega.plugins.webservice.handler.WebServiceCallContext;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.metamodel.WebServiceParam.ImplicitParam;
import io.vertigo.vega.webservice.validation.UiMessageStack;
import spark.Request;
import spark.Response;

public final class ImplicitJsonConverter implements JsonConverter, JsonSerializer {

	/** {@inheritDoc} */
	@Override
	public boolean canHandle(final Class<?> paramClass) {
		return UiMessageStack.class.isAssignableFrom(paramClass)
				|| HttpServletRequest.class.isAssignableFrom(paramClass)
				|| HttpServletResponse.class.isAssignableFrom(paramClass);
	}

	/** {@inheritDoc} */
	@Override
	public void populateWebServiceCallContext(final Object input, final WebServiceParam webServiceParam, final WebServiceCallContext routeContext) {
		Assertion.checkArgument(getSupportedInputs()[0].isInstance(input), "This JsonConverter doesn't support this input type {0}. Only {1} is supported", input.getClass().getSimpleName(), Arrays.toString(getSupportedInputs()));
		//-----
		final Object value = readImplicitValue((Request) input, webServiceParam, routeContext);
		routeContext.setParamValue(webServiceParam, value);
	}

	/** {@inheritDoc} */
	@Override
	public Class[] getSupportedInputs() {
		return new Class[] { Request.class };
	}

	private static Object readImplicitValue(final Request request, final WebServiceParam webServiceParam, final WebServiceCallContext routeContext) {
		switch (ImplicitParam.valueOf(webServiceParam.getName())) {
			case UiMessageStack:
				return routeContext.getUiMessageStack();
			case Request:
				return request.raw();
			case Response:
				return routeContext.getResponse().raw();
			default:
				throw new IllegalArgumentException("ImplicitParam : " + webServiceParam.getName());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toJson(final Object result, final Response response, final WebServiceDefinition webServiceDefinition) {
		Assertion.checkArgument(HttpServletResponse.class.isInstance(result), "This JsonConverter doesn't support this output type {0}. Only {1} is supported", result.getClass().getSimpleName(), HttpServletResponse.class.getSimpleName());
		//-----
		Assertion.checkState(((HttpServletResponse) result).isCommitted(), "The httpResponse returned wasn't close. Ensure you have close your streams.");
		//-----
		return ""; // response already send but can't send null : javaspark understand it as : not consumed here
	}

}
