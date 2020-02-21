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
package io.vertigo.vega.impl.webservice;

import java.util.Collection;

import io.vertigo.core.component.Plugin;
import io.vertigo.vega.plugins.webservice.handler.HandlerChain;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;

/**
 * Register an handlerchain as a route for this webService.
 * @author npiedeloup
 */
public interface WebServerPlugin extends Plugin {

	/**
	 * @param handlerChain HandlerChain of this route
	 * @param webServiceDefinitions WebServiceDefinitions to register
	 */
	void registerWebServiceRoute(HandlerChain handlerChain, Collection<WebServiceDefinition> webServiceDefinitions);

}
