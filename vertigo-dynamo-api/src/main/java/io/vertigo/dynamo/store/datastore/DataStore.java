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
package io.vertigo.dynamo.store.datastore;

import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.UID;

/**
 * Defines the way to acces and store all the data.
 * Les méthodes de mises à jour lacent des erreurs utilisateurs et techniques.
 * Les méthodes d'accès aux données ne lancent que des erreurs techniques.
 *
 * @author  pchretien
 */
public interface DataStore {
	/**
	 * Nombre d'éléments présents dans le sysème de persistance.
	 * @param dtDefinition Définition de DT
	 * @return Nombre d'éléments.
	 */
	int count(final DtDefinition dtDefinition);

	/**
	 * Récupération d'un objet persistant par son UID.
	 * Lorsque l'objet est en lecture seule il est possible d'accéder au objets partagés. (Liste de référence par ex)
	 * L'objet doit exister.
	 *
	 * @param <E> the type of entity
	 * @param uid UID de l'object
	 * @return object récupéré NOT NULL
	 */
	<E extends Entity> E readOne(final UID<E> uid);

	/**
	 * Récupération d'une liste identifiée par son UID.
	 *
	 * @param <E> the type of entity
	 * @param uid UID de la collection à récupérer
	 * @return DtList DTC
	 */
	<E extends Entity> DtList<E> findAll(final DtListURI uid);

	/**
	 * Loads and marks element for update, and ensure non concurrency.
	 * Fire an update event for this uid on eventbus after commit.
	 * @param <E> the type of entity
	 * @param uid UID of object
	 * @return object to update
	 */
	<E extends Entity> E readOneForUpdate(UID<E> uid);

	/**
	* Create an object.
	* No object with the same id must have been created previously.
	*
	* @param entity the entity to create
	* @return the created object
	*/
	<E extends Entity> E create(E entity);

	/**
	* Update an object.
	* This object must have an id.
	* @param entity the entity to update
	*/
	void update(Entity entity);

	/**
	 * Destruction d'un objet persistant par son UID.
	 *
	 * @param uid UID de l'objet à supprimer
	 */
	void delete(UID<? extends Entity> uid);

	/**
	 * Returns a list identified by criteria
	 * @param dtDefinition the list definition
	 * @param criteria criteria
	 * @param dtListState request state : sort, top, offset
	 * @return list
	 */
	<E extends Entity> DtList<E> find(final DtDefinition dtDefinition, Criteria<E> criteria, final DtListState dtListState);

	/**
	 * Return the a dedicated object that handles NN associations
	 * @return the handler for NN Associations
	 */
	BrokerNN getBrokerNN();

}
