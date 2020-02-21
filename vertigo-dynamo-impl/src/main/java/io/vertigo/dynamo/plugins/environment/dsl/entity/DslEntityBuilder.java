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
package io.vertigo.dynamo.plugins.environment.dsl.entity;

import java.util.HashSet;
import java.util.Set;

import io.vertigo.dynamo.plugins.environment.dsl.entity.DslEntityField.Cardinality;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * This class provides a common way to build an entity.
 *
 * @author pchretien
 */
public final class DslEntityBuilder implements Builder<DslEntity> {
	/**
	 * Name of the entity.
	 */
	private final String name;

	/**
	 * Fields of the entity.
	 */
	private final Set<DslEntityField> fields;

	private boolean myProvided;

	/**
	 * Constructor.
	 * @param name the name of the entity
	 */
	DslEntityBuilder(final String name) {
		Assertion.checkNotNull(name);
		//-----
		this.name = name;
		fields = new HashSet<>();

	}

	/**
	 * Set this entity as core and managed specificaly.
	 * @return this builder
	 */
	public DslEntityBuilder withProvided() {
		myProvided = true;
		return this;
	}

	/**
	 * Adds a new simple REQUIRED field.
	 * @param fieldName the name of the field
	 * @param type the type of the field
	 * @return this builder
	 */
	public DslEntityBuilder addRequiredField(final String fieldName, final DslEntityFieldType type) {
		return addField(fieldName, type, Cardinality.ONE);
	}

	/**
	 * Adds a new simple OPTIONAL field.
	 * @param fieldName the name of the field
	 * @param type the type of the field
	 * @return this builder
	 */
	public DslEntityBuilder addOptionalField(final String fieldName, final DslEntityFieldType type) {
		return addField(fieldName, type, Cardinality.OPTIONAL);
	}

	/**
	 * Adds a new multi field defined by an entity.
	 * @param fieldName the name of the field
	 * @param entity Type of the field
	 * @return this builder
	 */
	public DslEntityBuilder addManyFields(final String fieldName, final DslEntity entity) {
		//Only Entities may be multiple
		return addField(fieldName, entity, Cardinality.MANY);
	}

	/**
	 * Adds a new multi field defined by an entity.
	 * @param fieldName the name of the field
	 * @param entityLink Type of the field
	 * @return this builder
	 */
	public DslEntityBuilder addManyFields(final String fieldName, final DslEntityLink entityLink) {
		//Only Entities or  Link may be multiple
		return addField(fieldName, entityLink, Cardinality.MANY);
	}

	/**
	 * Adds a new field.
	 * @param fieldName the name of the field
	 * @param type the type of the field
	 * @param cardinality the cardinality of the field
	 * @return this builder
	 */
	private DslEntityBuilder addField(final String fieldName, final DslEntityFieldType type, final Cardinality cardinality) {
		final DslEntityField field = new DslEntityField(fieldName, type, cardinality);
		//-----
		fields.add(field);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public DslEntity build() {
		return new DslEntity(name, fields, myProvided);
	}
}
