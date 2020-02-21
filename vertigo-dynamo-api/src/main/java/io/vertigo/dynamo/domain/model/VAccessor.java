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
package io.vertigo.dynamo.domain.model;

/**
 * This class is a way to access an entity defined by a relationship.
 * It's a kind of box (aka optional) that offers a small list of methods.
 *
 * @author pchretien
 *
 * @param <E> the type of entity
 */
public final class VAccessor<E extends Entity> extends AbstractVAccessor<E> {

	private static final long serialVersionUID = -3620065166718209361L;

	/**
	 * Constructor.
	 * @param clazz the entity class
	 * @param role the role of the association (case of multiple associations with the same entity)
	 */
	public VAccessor(final Class<E> clazz, final String role) {
		super(clazz, role);
	}

}
