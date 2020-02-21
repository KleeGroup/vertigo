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

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.model.DtListDelta;
import io.vertigo.vega.webservice.validation.UiContextResolver;
import io.vertigo.vega.webservice.validation.UiMessageStack;
import io.vertigo.vega.webservice.validation.VegaUiMessageStack;
import spark.Request;
import spark.Response;

/**
* @author npiedeloup
*/
public final class WebServiceCallContext {
	private static final String UI_MESSAGE_STACK = "UiMessageStack";
	private final WebServiceDefinition webServiceDefinition;
	private final Request request;
	private final Response response;
	private final UiContextResolver uiContextResolver;

	/**
	 * Constructor.
	 * @param request Request
	 * @param response Response
	 * @param webServiceDefinition WebServiceDefinition
	 */
	public WebServiceCallContext(final Request request, final Response response, final WebServiceDefinition webServiceDefinition) {
		this.request = request;
		this.response = response;
		this.webServiceDefinition = webServiceDefinition;
		uiContextResolver = new UiContextResolver();
		request.attribute(UI_MESSAGE_STACK, new VegaUiMessageStack(uiContextResolver));
	}

	/**
	 * @return WebServiceDefinition
	 */
	public WebServiceDefinition getWebServiceDefinition() {
		return webServiceDefinition;
	}

	/**
	 * @return UiMessageStack
	 */
	public UiMessageStack getUiMessageStack() {
		return (UiMessageStack) request.attribute(UI_MESSAGE_STACK);
	}

	/**
	 * @return Request
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * @return Response
	 */
	public Response getResponse() {
		return response;
	}

	/**
	 * Set param of an endpoint.
	 * @param webServiceParam param name
	 * @param value param value
	 */
	public void setParamValue(final WebServiceParam webServiceParam, final Object value) {
		request.attribute(webServiceParam.getFullName(), ifOptional(webServiceParam, value));
	}

	/**
	 * Get param of an endpoint.
	 * @param webServiceParam param name
	 * @return param value
	 */
	public Object getParamValue(final WebServiceParam webServiceParam) {
		return request.attribute(webServiceParam.getFullName());
	}

	/**
	 * Register Updated Dtos.
	 * @param webServiceParam param name
	 * @param updatedValue param updatedvalue
	 * @param contextKeyMap Map of elements contextKey
	 */
	public void registerUpdatedDtObjects(final WebServiceParam webServiceParam, final Serializable updatedValue, final Map<String, DtObject> contextKeyMap) {
		Assertion.checkArgument(updatedValue instanceof DtObject
				|| updatedValue instanceof DtList
				|| updatedValue instanceof DtListDelta,
				"Context {0} format {1} not supported. Should be a DtObject, a DtList or a DtListDelta", webServiceParam.getFullName(), updatedValue.getClass().getSimpleName());

		for (final Map.Entry<String, DtObject> entry : contextKeyMap.entrySet()) {
			uiContextResolver.register(entry.getKey(), entry.getValue());
		}
		request.attribute(webServiceParam.getFullName() + "-input", request.attribute(webServiceParam.getFullName()));
		request.attribute(webServiceParam.getFullName(), ifOptional(webServiceParam, updatedValue));
	}

	private static Object ifOptional(final WebServiceParam webServiceParam, final Object value) {
		Object newValue = value;
		if (webServiceParam.isOptional()) {
			newValue = Optional.ofNullable(value);
		}
		return newValue;
	}
}
