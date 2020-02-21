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
package io.vertigo.dynamo.plugins.environment.dsl.dynamic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntity;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntityField;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * Interface de création des définitions.
 * @author  pchretien
 */
public final class DslDefinitionBuilder implements Builder<DslDefinition> {
	/** Type. */
	private final DslEntity entity;

	/** Name of the package. */
	private String packageName;

	/**name of this definition.*/
	private final String name;

	/** Map  (fieldName, propertyValue)  */
	private final Map<DslEntityField, Object> propertyValueByFieldName = new HashMap<>();

	/**
	 * Links.
	 * Map (fieldName, definitions identified by its name)
	 */

	private final Map<DslEntityField, List<String>> definitionLinkNamesByFieldName = new LinkedHashMap<>();

	/**
	 * Children.
	 * Map (fieldName, definitions
	 */
	private final Map<DslEntityField, List<DslDefinition>> childDefinitionsByFieldName = new LinkedHashMap<>();

	/**
	 * Constructor.
	 * @param name the name of the dslDefinition
	 * @param entity Entité
	 */
	DslDefinitionBuilder(final String name, final DslEntity entity) {
		Assertion.checkNotNull(name);
		Assertion.checkNotNull(entity);
		//-----
		this.name = name;
		this.entity = entity;
		for (final DslEntityField dslEntityField : entity.getFields()) {
			if (dslEntityField.getType().isEntityLink()) {
				definitionLinkNamesByFieldName.put(dslEntityField, new ArrayList<>());
			} else if (dslEntityField.getType().isEntity()) {
				childDefinitionsByFieldName.put(dslEntityField, new ArrayList<>());
			}
			// else : nothing for property
		}
	}

	public DslEntity getEntity() {
		return entity;
	}

	/**
	 * @param newPackageName Package name
	 * @return Builder
	 */
	public DslDefinitionBuilder withPackageName(final String newPackageName) {
		packageName = newPackageName;
		return this;
	}

	/**
	 * @param dslDefinition Definition body
	 * @return this builder
	 */
	public DslDefinitionBuilder merge(final DslDefinition dslDefinition) {
		if (packageName == null) {
			withPackageName(dslDefinition.getPackageName());
		}
		// 1. maj des EntityProperty
		for (final String propertyName : dslDefinition.getPropertyNames()) {
			addPropertyValue(propertyName, dslDefinition.getPropertyValue(propertyName));
		}

		for (final DslEntityField dslEntityField : entity.getFields()) {
			if (dslEntityField.getType().isEntityLink()) {
				// 2. link
				addAllDefinitionLinks(dslEntityField.getName(), dslDefinition.getDefinitionLinkNames(dslEntityField.getName()));
			} else if (dslEntityField.getType().isEntity()) {
				// 3. children
				addAllChildDefinitions(dslEntityField.getName(), dslDefinition.getChildDefinitions(dslEntityField.getName()));
			}
			// else : nothing for property (already processed)
		}
		return this;
	}

	/**
	 * @param fieldName Name of the field
	 * @param value Valeur de la propriété
	 * @return this builder
	 */
	public DslDefinitionBuilder addPropertyValue(final String fieldName, final Object value) {
		final DslEntityField dslEntityField = entity.getField(fieldName);
		Assertion.checkState(dslEntityField.getType().isProperty(), "expected a property on {0}", fieldName);
		//----
		entity.getPropertyType(fieldName).checkValue(value);
		propertyValueByFieldName.put(dslEntityField, value);
		return this;
	}

	/**
	 * Ajoute une définition au champ défini par fieldName.
	 * La définition n'est connue que par sa référence, son nom.
	 * @param fieldName Name of the field
	 * @param definitionName Name of the definition
	 * @return this builder
	 */
	public DslDefinitionBuilder addDefinitionLink(final String fieldName, final String definitionName) {
		return addAllDefinitionLinks(fieldName, Collections.singletonList(definitionName));
	}

	/**
	 * Ajoute une liste de définitions au champ défini par fieldName.
	 * La définition n'est connue que par sa référence, son nom.
	 * @param fieldName Name of the field
	 * @param definitionNames  list of the names of the dedinitions
	 * @return this builder
	 */
	public DslDefinitionBuilder addAllDefinitionLinks(final String fieldName, final List<String> definitionNames) {
		Assertion.checkNotNull(definitionNames);
		final DslEntityField dslEntityField = entity.getField(fieldName);
		Assertion.checkState(dslEntityField.getType().isEntityLink(), "expected a link on {0}", fieldName);
		//---
		definitionLinkNamesByFieldName.get(dslEntityField)
				.addAll(definitionNames);
		return this;
	}

	private void addAllChildDefinitions(final String fieldName, final List<DslDefinition> dslDefinitions) {
		Assertion.checkNotNull(dslDefinitions);
		final DslEntityField dslEntityField = entity.getField(fieldName);
		Assertion.checkState(dslEntityField.getType().isEntity(), "expected an entity on {0}", fieldName);
		//---
		childDefinitionsByFieldName.get(dslEntityField)
				.addAll(dslDefinitions);
	}

	/**
	 * Ajoute une définition au champ défini par fieldName.
	 * @param fieldName Name of the field
	 * @param definition Définition
	 * @return this builder
	 */
	public DslDefinitionBuilder addChildDefinition(final String fieldName, final DslDefinition definition) {
		Assertion.checkNotNull(definition);
		addAllChildDefinitions(fieldName, Collections.singletonList(definition));
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public DslDefinition build() {
		return new DslDefinition(entity, packageName, name, propertyValueByFieldName, definitionLinkNamesByFieldName, childDefinitionsByFieldName);
	}

}
