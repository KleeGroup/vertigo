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
package io.vertigo.core.node.definition;

import java.util.Set;

/**
 * Espace de définitions (non threadSafe).
 * @author mlaroche
 *
 */
public interface DefinitionSpace {

	/**
	 * Returns true if this container contains the specified definition
	 * @param name the name of the expected definition
	 * @return true if the definition is already registered.
	 */
	boolean contains(String name);

	/**
	 * Resolve a definition from its name and class.
	 * @param name the name of the expected definition
	 * @param clazz Type of the definition
	 * @return the definition
	 */
	<D extends Definition> D resolve(String name, Class<D> clazz);

	/**
	 * @return Liste de tous les types de définition gérés.
	 */
	Set<Class<? extends Definition>> getAllTypes();

	/**
	 * @return Ordered Set of all objects for a type defined by its class
	 * @param clazz Class of the definition
	 * @param <C> Type of the definition
	 */
	<C extends Definition> Set<C> getAll(Class<C> clazz);
}
