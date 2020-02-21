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
package io.vertigo.app;

import java.time.Instant;

import io.vertigo.app.config.NodeConfig;
import io.vertigo.core.component.ComponentSpace;
import io.vertigo.core.definition.DefinitionSpace;

/**
 * The app class is the core of vertigo.
 *
 * An app has a structure and contains all the components and definitions that provides services.
 *
 * Structure
 * |
 * |---DefinitionSpace
 * |     contains all the definitions
 * |---ComponentSpace
 * |     contains all the components including plugins and aspects
 *
 * An app has an internal lifecycle.
 *
 *
 * o--->[starting]--->[active]--->[stopping]--->[closed]
 *
 * If an error occured during the starting process then all the started components are stopped and the app is closed
 *
 * @author pchretien
 */
public interface App {

	/**
	 * @param postStartFunction Runnable function post start
	 */
	void registerPreActivateFunction(final Runnable postStartFunction);

	/**
	 * @return Start
	 */
	Instant getStart();

	/**
	 * @return Application configuration
	 */
	NodeConfig getNodeConfig();

	/**
	 * Returns the space where all the definitions are stored.
	 * @return the definitionSpace
	 */
	DefinitionSpace getDefinitionSpace();

	/**
	 * Returns the space where all the components are stored.
	 * @return the componentSpace
	 */
	ComponentSpace getComponentSpace();

}
