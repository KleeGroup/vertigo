/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node;

import java.time.Instant;

import io.vertigo.core.node.component.ComponentSpace;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.definition.DefinitionSpace;

/**
 * The node class is the core of vertigo.
 *
 * An node has a structure and contains all the components and definitions that provides services.
 *
 * Structure
 * |
 * |---DefinitionSpace
 * |     contains all the definitions
 * |---ComponentSpace
 * |     contains all the components including plugins and aspects
 *
 * A node has an internal lifecycle.
 *
 *
 * o--->[starting]--->[active]--->[stopping]--->[closed]
 *
 * If an error occured during the starting process then all the started components are stopped and the node is closed
 *
 * @author pchretien
 */
public interface Node {

	static Node getNode() {
		return AutoCloseableNode.getCurrentApp();
	}

	/**
	 * @param postStartFunction Runnable function post start
	 */
	void registerPreActivateFunction(final Runnable postStartFunction);

	/**
	 * @return Start
	 */
	Instant getStart();

	/**
	 * @return the node configuration
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
