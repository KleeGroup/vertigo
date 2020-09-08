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
package io.vertigo.core.node.component;

import io.vertigo.core.util.StringUtil;

/**
 * Centralisation des accès aux composants et aux plugins.
 * @author pchretien
 */
public interface ComponentSpace extends Container {
	/**
	 * Resolve a component from its class.
	 * @param componentClass Type of the component
	 * @return Component
	 */
	default <C> C resolve(final Class<C> componentClass) {
		final String normalizedId = StringUtil.first2LowerCase(componentClass.getSimpleName());
		return resolve(normalizedId, componentClass);
	}

}
