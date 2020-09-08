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
package io.vertigo.core.node.definition.loader;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.JsonExclude;
import io.vertigo.core.node.definition.Definition;
import io.vertigo.core.node.definition.DefinitionSpace;

/**
 * The space to access and register all the definitions.
 * The registration is only accessible during the boot phase.
 * Consequently, the registration is not threadSafe. (The boot phase occuring on a single thread)
 *
 * @author pchretien
 */
public final class DefinitionSpaceWritable implements DefinitionSpace {
	@JsonExclude
	private final Map<String, Definition> definitions = new LinkedHashMap<>();
	private final AtomicBoolean locked = new AtomicBoolean(false);

	public DefinitionSpaceWritable() {
		super();
	}

	/**
	 * Registers a new definition.
	 * The definition must not be already registered.
	 * @param definition the definition
	 */
	public void registerDefinition(final Definition definition) {
		Assertion.check()
				.isFalse(locked.get(), "Registration is now closed. A definition can be registerd only during the boot phase")
				.isNotNull(definition, "A definition can't be null.")
				.isFalse(definitions.containsKey(definition.getName()), "this definition '{0}' is already registered", definition.getName());
		//---
		definitions.put(definition.getName(), definition);
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String name) {
		return definitions.containsKey(name);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends Definition> D resolve(final String name, final Class<D> clazz) {
		Assertion.check()
				.isNotNull(name)
				.isNotNull(clazz);
		//-----
		final Definition definition = definitions.get(name);
		Assertion.check()
				.isNotNull(definition, "Definition '{0}' of type '{1}' not found in ({2})", name, clazz.getSimpleName(), definitions.keySet());
		return clazz.cast(definition);
	}

	/** {@inheritDoc} */
	@Override
	public Set<Class<? extends Definition>> getAllTypes() {
		return definitions.values()
				.stream()
				.map(Definition::getClass)
				.collect(Collectors.toSet());
	}

	/** {@inheritDoc} */
	@Override
	public <C extends Definition> Set<C> getAll(final Class<C> clazz) {
		Assertion.check().isNotNull(clazz); // Le type des objets recherchés ne peut pas être null
		//-----
		return definitions.values()
				.stream()
				.filter(definition -> clazz.isAssignableFrom(definition.getClass()))
				.map(clazz::cast)
				.sorted(Comparator.comparing(Definition::getName))
				.collect(Collectors.toSet());
	}

	/**
	 * Clears all known definitions
	 */
	public void clear() {
		definitions.clear();
	}

	/**
	 * Close registration of definitions.
	 * After calling this no more definitions can be loaded.
	 */
	void closeRegistration() {
		//registration is now closed.
		locked.set(true);
	}
}
