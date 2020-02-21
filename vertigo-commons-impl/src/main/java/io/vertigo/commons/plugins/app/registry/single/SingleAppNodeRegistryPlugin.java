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
package io.vertigo.commons.plugins.app.registry.single;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.vertigo.app.Home;
import io.vertigo.commons.app.Node;
import io.vertigo.commons.impl.app.AppNodeRegistryPlugin;
import io.vertigo.lang.Assertion;

/**
 * Memory implementation for a single node app.
 * @author mlaroche
 *
 */
public final class SingleAppNodeRegistryPlugin implements AppNodeRegistryPlugin {

	private Node localNode;

	@Override
	public void register(final Node node) {
		Assertion.checkState(localNode == null, "SingleNode has already been registered");
		// ---
		Assertion.checkNotNull(node);
		// ---
		localNode = node;

	}

	@Override
	public synchronized void unregister(final Node node) {
		localNode = null;
	}

	@Override
	public Optional<Node> find(final String nodeId) {
		if (Home.getApp().getNodeConfig().getNodeId().equals(nodeId)) {
			return Optional.of(localNode);
		}
		return Optional.empty();
	}

	@Override
	public void updateStatus(final Node node) {
		localNode = node;

	}

	@Override
	public List<Node> getTopology() {
		if (localNode != null) {
			return Collections.singletonList(localNode);
		}
		return Collections.emptyList();
	}

}
