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
package io.vertigo.core.node.component.loader;

import java.util.LinkedHashSet;
import java.util.Set;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.node.component.Container;

/**
 * This class is a 'virtual' container composed of two containers.
 *
 * @author pchretien
 */
final class ComponentDualContainer implements Container {
	private final Container container1;
	private final Container container2;
	private final Set<String> ids;

	ComponentDualContainer(final Container container1, final Container container2) {
		Assertion.check()
				.isNotNull(container1)
				.isNotNull(container2);
		//-----
		this.container1 = container1;
		this.container2 = container2;
		ids = new LinkedHashSet<>();
		ids.addAll(container1.keySet());
		ids.addAll(container2.keySet());
		Assertion.check()
				.isTrue(ids.size() == container1.keySet().size() + container2.keySet().size(), "Ambiguité : il y a des ids en doublon");
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String id) {
		Assertion.check()
				.isNotNull(id);
		//-----
		return ids.contains(id);
	}

	/** {@inheritDoc} */
	@Override
	public <O> O resolve(final String id, final Class<O> clazz) {
		Assertion.check()
				.isNotNull(id)
				.isNotNull(clazz);
		//-----
		if (container1.contains(id)) {
			return container1.resolve(id, clazz);
		}
		if (container2.contains(id)) {
			return container2.resolve(id, clazz);
		}
		throw new VSystemException("component info with id '{0}' not found.", id);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return ids;
	}
}
