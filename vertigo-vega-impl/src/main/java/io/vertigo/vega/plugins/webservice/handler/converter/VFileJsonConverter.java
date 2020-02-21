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

import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.plugins.webservice.handler.WebServiceCallContext;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import spark.Request;
import spark.Response;

public final class VFileJsonConverter implements JsonConverter, JsonSerializer {

	/** {@inheritDoc} */
	@Override
	public boolean canHandle(final Class<?> paramClass) {
		return VFile.class.isAssignableFrom(paramClass);
	}

	/** {@inheritDoc} */
	@Override
	public void populateWebServiceCallContext(final Object input, final WebServiceParam webServiceParam, final WebServiceCallContext routeContext) {
		Assertion.checkArgument(
				getSupportedInputs()[0].isInstance(input),
				"This JsonConverter doesn't support this input type {0}. Only {1} is supported", input.getClass().getSimpleName(), Arrays.toString(getSupportedInputs()));
		//-----
		final VFile value = VFileUtil.readVFileParam((Request) input, webServiceParam);
		routeContext.setParamValue(webServiceParam, value);
	}

	/** {@inheritDoc} */
	@Override
	public Class[] getSupportedInputs() {
		return new Class[] { Request.class };
	}

	/** {@inheritDoc} */
	@Override
	public String toJson(final Object result, final Response response, final WebServiceDefinition webServiceDefinition) {
		VFileUtil.sendVFile(result, webServiceDefinition.isFileAttachment(), response);
		return ""; // response already send but can't send null : javaspark understand it as : not consumed here
	}

}
