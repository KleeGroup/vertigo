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
package io.vertigo.core.definition;

import java.util.Collection;

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
	Collection<Class<? extends Definition>> getAllTypes();

	/**
	 * @return Collection de tous les objets enregistrés pour un type donné.
	 * @param clazz type de l'object
	 * @param <C> Type de l'objet
	 */
	<C extends Definition> Collection<C> getAll(Class<C> clazz);
}
