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

import io.vertigo.vega.plugins.webservice.handler.WebServiceCallContext;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;

/**
 * Converter source object into value object and put it into RouteContext.
 * @author npiedeloup
 */
public interface JsonConverter {

	/**
	 * @param paramClass Class to test
	 * @return If this converter can output this type of data.
	 */
	boolean canHandle(Class<?> paramClass);

	/**
	 * Converter source object into value object and put it into RouteContext.
	 * @param source Source
	 * @param webServiceParam Param
	 * @param routeContext RouteContext
	 */
	void populateWebServiceCallContext(Object source, WebServiceParam webServiceParam, WebServiceCallContext routeContext);

	/**
	 * @return Input types
	 */
	Class<?>[] getSupportedInputs();

}
