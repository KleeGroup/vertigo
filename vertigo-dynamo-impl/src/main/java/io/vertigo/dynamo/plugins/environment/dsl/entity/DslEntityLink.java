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
 * Defines a link to an entity.
 * @author pchretien
 */
public final class DslEntityLink implements DslEntityFieldType {
	private final DslEntity entity;

	/**
	 * Constructor
	 * @param entity the entity that is linked
	 */
	DslEntityLink(final DslEntity entity) {
		Assertion.checkNotNull(entity);
		//-----
		this.entity = entity;
	}

	/**
	 * @return the linked entity
	 */
	public DslEntity getEntity() {
		return entity;
	}

	@Override
	public String toString() {
		return "Link<" + entity.getName() + ">";
	}

	@Override
	public boolean isProperty() {
		return false;
	}

	@Override
	public boolean isEntityLink() {
		return true;
	}

	@Override
	public boolean isEntity() {
		return false;
	}
}
