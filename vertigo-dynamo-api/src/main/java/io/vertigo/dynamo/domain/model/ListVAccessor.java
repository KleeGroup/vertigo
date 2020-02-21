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
import java.util.stream.Stream;

import io.vertigo.app.Home;
import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForAssociation;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForSimpleAssociation;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

/**
 * This class is a way to access to a list of entities managed by a relationship.
 * It's a kind of box (aka optional) that offers a small list of methods.
 *
 * @author mlaroche
 *
 * @param <E> the type of the targetedEntity
 */
public final class ListVAccessor<E extends Entity> implements Serializable {
	private static final long serialVersionUID = 1L;

	private static enum State {
		LOADED, NOT_LOADED
	}

	private State status = State.NOT_LOADED;
	private final Entity entity;
	private final DefinitionReference<AssociationDefinition> associationDefinitionReference;
	private final DefinitionReference<DtDefinition> targetDefinitionReference;
	private final String roleName;
	private DtList<E> value;

	/**
	 * Constructor.
	 * @param entity the entity
	 * @param roleName the role of the association (case of multiple associations with the same entity)
	 */
	public ListVAccessor(final Entity entity, final String associationDefinitionName, final String roleName) {
		Assertion.checkNotNull(entity);
		Assertion.checkArgNotEmpty(associationDefinitionName);
		Assertion.checkArgNotEmpty(roleName);
		//---
		this.entity = entity;
		this.roleName = roleName;
		//---
		final AssociationDefinition associationDefinition = Home.getApp().getDefinitionSpace().resolve(associationDefinitionName, AssociationDefinition.class);
		this.associationDefinitionReference = new DefinitionReference<>(associationDefinition);
		final DtDefinition targetDefinition = Stream.of(associationDefinition.getAssociationNodeA(), associationDefinition.getAssociationNodeB())
				.filter(associationNode -> roleName.equals(associationNode.getRole()))
				.findFirst()
				.orElseThrow(() -> new VSystemException("Unable to find association node with role '{1}' on association '{0}'", associationDefinitionName, roleName))
				.getDtDefinition();
		//---
		this.targetDefinitionReference = new DefinitionReference<>(targetDefinition);
	}

	private static DataStore getDataStore() {
		return io.vertigo.app.Home.getApp().getComponentSpace().resolve(io.vertigo.dynamo.store.StoreManager.class)
				.getDataStore();
	}

	/**
	 * @return the entity uri
	 */
	public final DtListURIForAssociation getDtListURI() {
		final AssociationDefinition associationDefinition = associationDefinitionReference.get();
		if (associationDefinition instanceof AssociationSimpleDefinition) {
			return new DtListURIForSimpleAssociation((AssociationSimpleDefinition) associationDefinition, entity.getUID(), roleName);
		} else if (associationDefinition instanceof AssociationNNDefinition) {
			return new DtListURIForNNAssociation((AssociationNNDefinition) associationDefinition, entity.getUID(), roleName);
		}
		throw new VSystemException("Unhandled type of association. Only Simple and NN Associations are supported");
	}

	/**
	 * Loads the value if needed.
	 */
	public final void load() {
		// we are not lazy the uid of the parent might have changed
		if (entity.getUID() != null) {
			value = getDataStore().findAll(getDtListURI());
		} else {
			// if the uid is null we return an empty dtList
			value = new DtList<>(targetDefinitionReference.get());
		}
		status = State.LOADED;
	}

	/**
	 * Loads the value if needed.
	 */
	public final DtList<E> get() {
		Assertion.checkState(status == State.LOADED, "Accessor is not loaded, you must load it before calling get method");
		//--
		return value;
	}

	/**
	 * Loads the value if needed.
	 */
	public final void reset() {
		status = State.NOT_LOADED;
		value = null;
	}

	/**
	 * @return if entity is already loaded
	 */
	public final boolean isLoaded() {
		return status == State.LOADED;
	}

	/**
	 * @return Role of this relation
	 */
	public final String getRole() {
		return roleName;
	}
}
