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

import io.vertigo.lang.Assertion;

/**
 * Field of an entity.
 *
 * @author pchretien
 */
public final class DslEntityField {

	/** Cardinalities of associations. */
	public enum Cardinality {
		/* 0  or 1*/
		OPTIONAL,
		/* 1 */
		ONE,
		/* 0..n */
		MANY
	}

	private final String name;
	private final Cardinality cardinality;
	private final DslEntityFieldType type;

	/**
	 * Constructor.
	 * @param name Name
	 * @param type Type of the entity
	 */
	DslEntityField(final String name, final DslEntityFieldType type, final Cardinality cardinality) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(type);
		Assertion.checkNotNull(cardinality);
		//-----
		this.name = name;
		this.cardinality = cardinality;
		this.type = type;
	}

	/**
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the cardinality
	 */
	public Cardinality getCardinality() {
		return cardinality;
	}

	/**
	 * @return the type of the entity
	 */
	public DslEntityFieldType getType() {
		return type;
	}
}
