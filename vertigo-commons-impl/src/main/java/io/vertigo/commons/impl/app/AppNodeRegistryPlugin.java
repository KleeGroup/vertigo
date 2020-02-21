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
package io.vertigo.commons.impl.app;

import java.util.List;
import java.util.Optional;

import io.vertigo.commons.app.Node;
import io.vertigo.core.component.Plugin;

/**
 * Plugin for storing and querying the node topology of an App.
 * @author mlaroche
 *
 */
public interface AppNodeRegistryPlugin extends Plugin {

	/**
	 * Register a node
	 * @param node the node to register
	 */
	void register(Node node);

	/**
	 * Unregister a node
	 * @param node the node to unregister
	 */
	void unregister(Node node);

	/**
	 * Get the whole topology of the app
	 * @return the list of node of the app
	 */
	List<Node> getTopology();

	/**
	 * Find a node in the topology with the given id
	 * @param nodeId the id to look for
	 * @return an optional Node
	 */
	Optional<Node> find(String nodeId);

	/**
	 * Update the status of a node
	 * @param node the node to update
	 */
	void updateStatus(Node node);

}
