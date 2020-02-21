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

import java.io.Serializable;

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.lang.Assertion;

/**
 * This class is a way to access an entity defined by a relationship.
 * It's a kind of box (aka optional) that offers a small list of methods.
 *
 * @author pchretien
 *
 * @param <E> the type of entity
 */
public abstract class AbstractVAccessor<E extends Entity> implements Serializable {
	private static final long serialVersionUID = 1L;

	private static enum State {
		LOADED, NOT_LOADED
	}

	private State status = State.NOT_LOADED;
	private final DefinitionReference<DtDefinition> targetDtDefinitionRef;
	private final String role;
	private UID<E> targetURI;
	private E value;

	/**
	 * Constructor.
	 * @param clazz the entity class
	 * @param role the role of the association (case of multiple associations with the same entity)
	 */
	AbstractVAccessor(final Class<E> clazz, final String role) {
		this(DtObjectUtil.findDtDefinition(clazz), role);
	}

	/**
	 * Constructor.
	 * @param targetDtDefinition the entity definition
	 * @param role the role of the association (case of multiple associations with the same entity)
	 */
	AbstractVAccessor(final DtDefinition targetDtDefinition, final String role) {
		Assertion.checkNotNull(targetDtDefinition);
		Assertion.checkArgNotEmpty(role);
		//---
		this.targetDtDefinitionRef = new DefinitionReference(targetDtDefinition);
		this.role = role;
	}

	private static DataStore getDataStore() {
		return io.vertigo.app.Home.getApp().getComponentSpace().resolve(io.vertigo.dynamo.store.StoreManager.class)
				.getDataStore();
	}

	/**
	 * @return the entity
	 */
	public final E get() {
		Assertion.checkState(status == State.LOADED, "Accessor is not loaded, you must load it before calling get method");
		//---
		return value;
	}

	/**
	 * @return the entity uid
	 */
	public final UID<E> getUID() {
		return targetURI;
	}

	/**
	 * @return the entity id
	 */
	public final Serializable getId() {
		return targetURI == null ? null : targetURI.getId();
	}

	/**
	 * Loads the value if needed.
	 */
	public void load() {
		value = targetURI == null ? null : getDataStore().readOne(targetURI);
		status = State.LOADED;
	}

	/**
	 * Sets the entity
	 * @param entity the entity
	 */
	public final void set(final E entity) {
		Assertion.checkNotNull(entity);
		//---
		value = entity;
		targetURI = entity.getUID();
		status = State.LOADED;
	}

	/**
	 * Sets the entity id
	 * @param id the entity id
	 */
	public final void setId(final Serializable id) {
		//id final may be null
		//---
		//If already loaded and same id, we don't touch anything
		if (!(status == State.LOADED && isSameId(id))) {
			targetURI = id == null ? null : UID.of(targetDtDefinitionRef.get(), id);
			//we have to reset the value and the state
			value = null;
			status = State.NOT_LOADED;
		}
	}

	private boolean isSameId(final Serializable id) {
		if (targetURI == null) {
			return id == null;
		}
		return targetURI.getId().equals(id);
	}

	/**
	 * Sets the entity uri
	 * @param uid the entity uri
	 */
	public final void setUID(final UID<E> uid) {
		Assertion.checkNotNull(uid);
		//---
		targetURI = uid; //maybe null
		//we have to reset the value and the state
		value = null;
		status = State.NOT_LOADED;
	}

	/**
	 * @return Role of this relation
	 */
	public final String getRole() {
		return role;
	}

	/**
	 * @return if entity is already loaded
	 */
	public final boolean isLoaded() {
		return status == State.LOADED;
	}
}
