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

import io.vertigo.core.lang.Assertion;

public abstract class AbstractDefinition implements Definition {
	private final String prefix;
	private final String name;

	protected AbstractDefinition(final String name) {
		final DefinitionPrefix definitionPrefix = this.getClass().getAnnotation(DefinitionPrefix.class);
		Assertion.check().isNotNull(definitionPrefix, "Annotation '@DefinitionPrefix' not found on {0}", this.getClass().getName());
		prefix = definitionPrefix.value();
		//---
		Assertion.check()
				.isNotBlank(prefix)
				.isNotBlank(name)
				.isTrue(name.startsWith(prefix), "La définition {0} doit commencer par {1}", name, prefix)
				.isTrue(name.length() > prefix.length(), "Le nom de la définition doit être renseigné")
				.isTrue(Character.isUpperCase(name.charAt(prefix.length())), "the name of the dtDefinition {0} must be in UpperCamelCase", name)
				.isTrue(REGEX_DEFINITION_NAME.matcher(name).matches(), "urn de définition {0} doit matcher le pattern {1}", name, Definition.REGEX_DEFINITION_NAME);
		//---
		this.name = name;
	}

	/** {@inheritDoc} */
	@Override
	public final String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public final String getLocalName() {
		return getName().substring(prefix.length());
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return name;
	}

}
