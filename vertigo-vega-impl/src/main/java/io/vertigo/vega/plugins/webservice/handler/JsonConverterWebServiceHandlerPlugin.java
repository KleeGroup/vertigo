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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonSyntaxException;

import io.vertigo.lang.Assertion;
import io.vertigo.util.InjectorUtil;
import io.vertigo.vega.engines.webservice.json.JsonEngine;
import io.vertigo.vega.impl.webservice.WebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.converter.DefaultJsonConverter;
import io.vertigo.vega.plugins.webservice.handler.converter.DefaultJsonSerializer;
import io.vertigo.vega.plugins.webservice.handler.converter.DtListDeltaJsonConverter;
import io.vertigo.vega.plugins.webservice.handler.converter.DtListJsonConverter;
import io.vertigo.vega.plugins.webservice.handler.converter.DtObjectJsonConverter;
import io.vertigo.vega.plugins.webservice.handler.converter.ImplicitJsonConverter;
import io.vertigo.vega.plugins.webservice.handler.converter.JsonConverter;
import io.vertigo.vega.plugins.webservice.handler.converter.JsonSerializer;
import io.vertigo.vega.plugins.webservice.handler.converter.PrimitiveJsonConverter;
import io.vertigo.vega.plugins.webservice.handler.converter.StringJsonSerializer;
import io.vertigo.vega.plugins.webservice.handler.converter.VFileJsonConverter;
import io.vertigo.vega.plugins.webservice.handler.reader.BodyJsonReader;
import io.vertigo.vega.plugins.webservice.handler.reader.HeaderJsonReader;
import io.vertigo.vega.plugins.webservice.handler.reader.InnerBodyJsonReader;
import io.vertigo.vega.plugins.webservice.handler.reader.JsonReader;
import io.vertigo.vega.plugins.webservice.handler.reader.PathJsonReader;
import io.vertigo.vega.plugins.webservice.handler.reader.QueryJsonReader;
import io.vertigo.vega.plugins.webservice.handler.reader.RequestJsonReader;
import io.vertigo.vega.webservice.exception.SessionException;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.metamodel.WebServiceParam.WebServiceParamType;
import spark.Request;
import spark.Response;

/**
 * Params handler.
 * It's an handler barrier : bellow this handler anything is object, over this handler it's json.
 * Extract and Json convert.
 * @author npiedeloup
 */
public final class JsonConverterWebServiceHandlerPlugin implements WebServiceHandlerPlugin {
	private static final Class<? extends JsonConverter>[] JSON_CONVERTER_CLASSES = new Class[] {
			ImplicitJsonConverter.class, PrimitiveJsonConverter.class,
			DtListJsonConverter.class, DtObjectJsonConverter.class, DtListDeltaJsonConverter.class,
			VFileJsonConverter.class, DefaultJsonConverter.class };
	private static final Class<? extends JsonReader<?>>[] JSON_READER_CLASSES = new Class[] {
			BodyJsonReader.class, InnerBodyJsonReader.class, HeaderJsonReader.class,
			PathJsonReader.class, QueryJsonReader.class, RequestJsonReader.class };

	private static final Class<? extends JsonSerializer>[] JSON_SERIALIZER_CLASSES = new Class[] {
			VFileJsonConverter.class, ImplicitJsonConverter.class, StringJsonSerializer.class,
			DefaultJsonSerializer.class };

	private final Map<Class, List<JsonConverter>> jsonConverters = new HashMap<>();
	private final EnumMap<WebServiceParamType, List<JsonReader<?>>> jsonReaders = new EnumMap<>(WebServiceParamType.class);
	private final List<JsonSerializer> jsonWriters = new ArrayList<>();

	/**
	* @param jsonReaderEngine jsonReaderEngine
	*/
	@Inject
	public JsonConverterWebServiceHandlerPlugin(final JsonEngine jsonReaderEngine) {
		Assertion.checkNotNull(jsonReaderEngine);
		//-----
		for (final Class<? extends JsonConverter> jsonConverterClass : JSON_CONVERTER_CLASSES) {
			final JsonConverter jsonConverter = InjectorUtil.newInstance(jsonConverterClass);
			for (final Class inputType : jsonConverter.getSupportedInputs()) {
				jsonConverters.computeIfAbsent(inputType, k -> new ArrayList<>())
						.add(jsonConverter);
			}
		}

		for (final Class<? extends JsonReader<?>> jsonReaderClass : JSON_READER_CLASSES) {
			final JsonReader<?> jsonReader = InjectorUtil.newInstance(jsonReaderClass);
			for (final WebServiceParamType restParamType : jsonReader.getSupportedInput()) {
				List<JsonReader<?>> jsonReaderByRestParamType = jsonReaders.get(restParamType);
				if (jsonReaderByRestParamType == null) {
					jsonReaderByRestParamType = new ArrayList<>();
					jsonReaders.put(restParamType, jsonReaderByRestParamType);
				}
				jsonReaderByRestParamType.add(jsonReader);
			}
		}
		for (final Class<? extends JsonSerializer> jsonSerializerClass : JSON_SERIALIZER_CLASSES) {
			final JsonSerializer jsonSerializer = InjectorUtil.newInstance(jsonSerializerClass);
			jsonWriters.add(jsonSerializer);
		}

	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final WebServiceDefinition webServiceDefinition) {
		return true;
	}

	/** {@inheritDoc}  */
	@Override
	public Object handle(final Request request, final Response response, final WebServiceCallContext routeContext, final HandlerChain chain) throws SessionException {
		//we can't read body at first : because if it's a multipart request call body() disabled getParts() access.
		for (final WebServiceParam webServiceParam : routeContext.getWebServiceDefinition().getWebServiceParams()) {
			readParameterValue(request, routeContext, webServiceParam);
		}
		final Object result = chain.handle(request, response, routeContext);
		return convertResultToJson(result, response, routeContext);
	}

	private void readParameterValue(final Request request, final WebServiceCallContext routeContext, final WebServiceParam webServiceParam) {
		try {
			boolean found = false;
			JsonReader jsonReaderToApply = null;
			JsonConverter jsonConverterToApply = null;
			for (final JsonReader jsonReader : jsonReaders.get(webServiceParam.getParamType())) {
				jsonReaderToApply = jsonReader;

				for (final JsonConverter jsonConverter : jsonConverters.get(jsonReader.getSupportedOutput())) {

					if (jsonConverter.canHandle(webServiceParam.getType())) {
						jsonConverterToApply = jsonConverter;
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
			//-----
			Assertion.checkNotNull(jsonReaderToApply,
					"Can't parse param {0} of service {1} {2} no compatible JsonReader found for {3}",
					webServiceParam.getFullName(),
					routeContext.getWebServiceDefinition().getVerb(),
					routeContext.getWebServiceDefinition().getPath(),
					webServiceParam.getParamType());
			Assertion.checkNotNull(jsonConverterToApply,
					"Can't parse param {0} of service {1} {2} no compatible JsonConverter found for {3} {4}",
					webServiceParam.getFullName(),
					routeContext.getWebServiceDefinition().getVerb(),
					routeContext.getWebServiceDefinition().getPath(),
					webServiceParam.getParamType(),
					webServiceParam.getType());
			//-----
			final Object converterSource = jsonReaderToApply.extractData(request, webServiceParam, routeContext);
			if (converterSource != null) { //On ne convertit pas les null
				jsonConverterToApply.populateWebServiceCallContext(converterSource, webServiceParam, routeContext);
			} else if (webServiceParam.isOptional()) {
				routeContext.setParamValue(webServiceParam, null /*converterSource*/);
			}
			Assertion.checkNotNull(routeContext.getParamValue(webServiceParam), "RestParam not found : {0}", webServiceParam);
		} catch (final JsonSyntaxException e) {
			throw new JsonSyntaxException("Error parsing param " + webServiceParam.getFullName() + " on service " + routeContext.getWebServiceDefinition().getVerb() + " " + routeContext.getWebServiceDefinition().getPath(), e);
		}
	}

	private String convertResultToJson(final Object result, final Response response, final WebServiceCallContext routeContext) {
		//optimize most common case
		if (result == null) {
			//if status was not set, or set to OK we set it to NO_CONTENT
			if (response.raw().getStatus() == HttpServletResponse.SC_OK || response.raw().getStatus() == 0) {
				response.status(HttpServletResponse.SC_NO_CONTENT);
			}
			return ""; //jetty understand null as 404 not found
		}

		JsonSerializer jsonWriterToApply = null;
		for (final JsonSerializer jsonWriter : jsonWriters) {
			if (jsonWriter.canHandle(result.getClass())) {
				jsonWriterToApply = jsonWriter;
				break;
			}
		}
		//-----
		Assertion.checkNotNull(jsonWriterToApply, "Can't send result of service {0} {1} no compatible JsonConverter found for {2}", routeContext.getWebServiceDefinition().getVerb(), routeContext.getWebServiceDefinition().getPath(), result.getClass().getName());
		final String json = jsonWriterToApply.toJson(result, response, routeContext.getWebServiceDefinition());
		Assertion.checkNotNull(json, "Can't convert result to json");
		return json;
	}
}
