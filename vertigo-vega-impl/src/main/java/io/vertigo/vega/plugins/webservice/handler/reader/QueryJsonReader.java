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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.engines.webservice.json.JsonEngine;
import io.vertigo.vega.plugins.webservice.handler.WebServiceCallContext;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.metamodel.WebServiceParam.WebServiceParamType;
import spark.QueryParamsMap;
import spark.Request;

public final class QueryJsonReader implements JsonReader<String> {

	private final JsonEngine jsonWriterEngine;

	/**
	 * @param jsonWriterEngine jsonWriterEngine
	 */
	@Inject
	public QueryJsonReader(final JsonEngine jsonWriterEngine) {
		Assertion.checkNotNull(jsonWriterEngine);
		//-----
		this.jsonWriterEngine = jsonWriterEngine;
	}

	/** {@inheritDoc} */
	@Override
	public WebServiceParamType[] getSupportedInput() {
		return new WebServiceParamType[] { WebServiceParamType.Query };
	}

	/** {@inheritDoc} */
	@Override
	public Class<String> getSupportedOutput() {
		return String.class;
	}

	/** {@inheritDoc} */
	@Override
	public String extractData(final Request request, final WebServiceParam webServiceParam, final WebServiceCallContext routeContext) {
		Assertion.checkArgument(
				getSupportedInput()[0].equals(webServiceParam.getParamType()),
				"This JsonReader can't read the asked request ParamType {0}. Only {1} is supported", webServiceParam.getParamType(), Arrays.toString(getSupportedInput()));
		//-----
		return readQueryValue(request.queryMap(), webServiceParam);
	}

	private String readQueryValue(final QueryParamsMap queryMap, final WebServiceParam webServiceParam) {
		final Class<?> paramClass = webServiceParam.getType();
		final String paramName = webServiceParam.getName();
		if (queryMap == null) {
			return null;
		}
		if (DtListState.class.isAssignableFrom(paramClass)
				|| DtObject.class.isAssignableFrom(paramClass)) {
			return convertToJson(queryMap, webServiceParam.getName());
		}
		return queryMap.get(paramName).value();
	}

	private String convertToJson(final QueryParamsMap queryMap, final String queryPrefix) {
		final String checkedQueryPrefix = queryPrefix.isEmpty() ? "" : queryPrefix + ".";
		final Map<String, Object> queryParams = new HashMap<>();
		for (final Entry<String, String[]> entry : queryMap.toMap().entrySet()) {
			if (entry.getKey().startsWith(checkedQueryPrefix)) {
				final String[] value = entry.getValue();
				final Object simplerValue = value.length == 0 ? null : value.length == 1 ? value[0] : value;
				queryParams.put(entry.getKey().substring(checkedQueryPrefix.length()), simplerValue);
			}
		}
		return jsonWriterEngine.toJson(queryParams);
	}
}
