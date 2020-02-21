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

import io.vertigo.vega.plugins.webservice.handler.WebServiceCallContext;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.metamodel.WebServiceParam.WebServiceParamType;
import spark.Request;

/**
 * Read request to extract a not converted parameter.
 * @author npiedeloup
 * @param <O> Output type
 */
public interface JsonReader<O> {

	/**
	 * @return Supported type of parameter in request
	 */
	WebServiceParamType[] getSupportedInput();

	/**
	 * @return Output classe supported
	 */
	Class<O> getSupportedOutput();

	/**
	 * Extract parameter value from request as readType.
	 * This doesn't convert it to value object, it's only extraction, the converter do the convert task.
	 * @param request Request
	 * @param webServiceParam Param infos
	 * @param routeContext routeContext
	 * @return output value
	 */
	O extractData(Request request, WebServiceParam webServiceParam, WebServiceCallContext routeContext);

}
