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
package io.vertigo.vega.plugins.webservice.handler.reader;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.vertigo.lang.Assertion;
import io.vertigo.vega.engines.webservice.json.JsonEngine;
import io.vertigo.vega.engines.webservice.json.UiContext;
import io.vertigo.vega.plugins.webservice.handler.WebServiceCallContext;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.metamodel.WebServiceParam.WebServiceParamType;
import spark.Request;

public final class InnerBodyJsonReader implements JsonReader<UiContext> {

	private final JsonEngine jsonReaderEngine;

	/**
	 * @param jsonReaderEngine jsonReaderEngine
	 */
	@Inject
	public InnerBodyJsonReader(final JsonEngine jsonReaderEngine) {
		Assertion.checkNotNull(jsonReaderEngine);
		//-----
		this.jsonReaderEngine = jsonReaderEngine;
	}

	/** {@inheritDoc} */
	@Override
	public WebServiceParamType[] getSupportedInput() {
		return new WebServiceParamType[] { WebServiceParamType.InnerBody };
	}

	/** {@inheritDoc} */
	@Override
	public Class<UiContext> getSupportedOutput() {
		return UiContext.class;
	}

	/** {@inheritDoc} */
	@Override
	public UiContext extractData(final Request request, final WebServiceParam webServiceParam, final WebServiceCallContext routeContext) {
		Assertion.checkArgument(
				getSupportedInput()[0].equals(webServiceParam.getParamType()),
				"This JsonReader can't read the asked request ParamType {0}. Only {1} is supported", webServiceParam.getParamType(), Arrays.toString(getSupportedInput()));
		//-----
		UiContext uiContext = routeContext.getRequest().attribute("InnerBodyValues");
		if (uiContext == null) {
			uiContext = readInnerBodyValue(request.body(), routeContext.getWebServiceDefinition().getWebServiceParams());
			routeContext.getRequest().attribute("InnerBodyValues", uiContext);
		}
		return uiContext;
	}

	private UiContext readInnerBodyValue(final String jsonBody, final List<WebServiceParam> webServiceParams) {
		final Map<String, Type> innerBodyParams = new HashMap<>();
		for (final WebServiceParam webServiceParam : webServiceParams) {
			if (webServiceParam.getParamType() == WebServiceParamType.InnerBody || webServiceParam.getParamType() == WebServiceParamType.Implicit) {
				innerBodyParams.put(webServiceParam.getName(), webServiceParam.getGenericType());
			}
		}
		return jsonReaderEngine.uiContextFromJson(jsonBody, innerBodyParams);
	}

}
