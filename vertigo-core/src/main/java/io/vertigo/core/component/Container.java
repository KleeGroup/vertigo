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
package io.vertigo.core.component;

import java.util.Set;

/**
 * The Container interface defines a universal container for the components.
 * Each component is identified by an id.
 *
 * @author pchretien
 */
public interface Container {

	/**
	 * Returns true if this container contains the specified component
	 * @param id Id of the component
	 * @return true if the component is already registered.
	 */
	boolean contains(final String id);

	/**
	 * Resolve a component from its id and class.
	 * @param id Id of the component
	 * @param componentClass Type of the component
	 * @return Component
	 */
	<T> T resolve(final String id, final Class<T> componentClass);

	/**
	 * Returns the list of the ids of the components managed in this container.
	 * @return list of ids
	 */
	Set<String> keySet();
}
