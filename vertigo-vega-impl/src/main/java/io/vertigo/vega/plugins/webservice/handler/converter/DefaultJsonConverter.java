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

import javax.inject.Inject;

import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.engines.webservice.json.JsonEngine;
import io.vertigo.vega.engines.webservice.json.UiContext;
import io.vertigo.vega.engines.webservice.json.UiListDelta;
import io.vertigo.vega.plugins.webservice.handler.WebServiceCallContext;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;

/**
 * Default JsonConverter.
 * @author npiedeloup
 */
public final class DefaultJsonConverter implements JsonConverter {

	private final JsonEngine jsonReaderEngine;

	/**
	 * @param jsonReaderEngine jsonReaderEngine
	 */
	@Inject
	public DefaultJsonConverter(final JsonEngine jsonReaderEngine) {
		Assertion.checkNotNull(jsonReaderEngine);
		//-----
		this.jsonReaderEngine = jsonReaderEngine;
	}

	/** {@inheritDoc} */
	@Override
	public boolean canHandle(final Class<?> paramClass) {
		return !VFile.class.isAssignableFrom(paramClass);
	}

	/** {@inheritDoc}*/
	@Override
	public void populateWebServiceCallContext(final Object input, final WebServiceParam webServiceParam, final WebServiceCallContext routeContext) {
		Assertion.checkNotNull(input, "This JsonConverter doesn't support null input");
		Assertion.checkArgument(
				getSupportedInputs()[0].isInstance(input) || getSupportedInputs()[1].isInstance(input),
				"This JsonConverter doesn't support this input type {0}. Only {1} is supported", input.getClass().getSimpleName(), Arrays.toString(getSupportedInputs()));
		//-----
		final Class<?> paramClass = webServiceParam.getType();
		final Object value;
		if (input instanceof String) {
			value = jsonReaderEngine.fromJson((String) input, webServiceParam.getGenericType());
		} else if (input instanceof UiContext) {
			value = ((UiContext) input).get(webServiceParam.getName());
		} else {
			throw new IllegalArgumentException(String.format("This JsonConverter can't read the asked type %s. Only %s is supported", paramClass.getSimpleName(), UiListDelta.class.getSimpleName()));
		} //-----
		routeContext.setParamValue(webServiceParam, value);
	}

	/** {@inheritDoc} */
	@Override
	public Class[] getSupportedInputs() {
		return new Class[] { String.class, UiContext.class };
	}
}
