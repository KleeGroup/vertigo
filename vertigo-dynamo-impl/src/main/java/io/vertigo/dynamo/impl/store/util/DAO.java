/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.impl.store.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.vertigo.dynamo.domain.metamodel.DataAccessor;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.Fragment;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.criteria.Criteria;
import io.vertigo.dynamo.store.criteria.FilterCriteria;
import io.vertigo.dynamo.store.criteria.FilterCriteriaBuilder;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.lang.Assertion;

/**
 * Classe utilitaire pour accéder au Broker.
 *
 * @author cgodard
 * @param <E> the type of entity
 * @param <P> Type de la clef primaire.
 */
public class DAO<E extends Entity, P> implements BrokerNN {

	/** DT de l'objet dont on gére le CRUD. */
	private final DtDefinition dtDefinition;
	protected final DataStore dataStore;
	private final BrokerNN brokerNN;
	private final BrokerBatch<E, P> brokerBatch;
	private final TaskManager taskManager;

	/**
	 * Contructeur.
	 *
	 * @param entityClass Définition du DtObject associé à ce DAO
	 * @param storeManager Manager de gestion de la persistance
	 * @param taskManager Manager de gestion des tâches
	 */
	public DAO(final Class<? extends Entity> entityClass, final StoreManager storeManager, final TaskManager taskManager) {
		this(DtObjectUtil.findDtDefinition(entityClass), storeManager, taskManager);
	}

	/**
	 * Contructeur.
	 *
	 * @param dtDefinition Définition du DtObject associé à ce DAO
	 * @param storeManager Manager de gestion de la persistance
	 * @param taskManager Manager de gestion des tâches
	 */
	public DAO(final DtDefinition dtDefinition, final StoreManager storeManager, final TaskManager taskManager) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(storeManager);
		Assertion.checkNotNull(taskManager);
		//-----
		dataStore = storeManager.getDataStore();
		brokerNN = new BrokerNNImpl(taskManager);
		this.dtDefinition = dtDefinition;
		brokerBatch = new BrokerBatchImpl<>(taskManager);
		this.taskManager = taskManager;
	}

	protected final TaskManager getTaskManager() {
		return taskManager;
	}

	public BrokerBatch<E, P> getBatch() {
		return brokerBatch;
	}

	/**
	 * Save an object.
	 *
	 * @param entity Object to save
	 */
	public final void save(final E entity) {
		if (DtObjectUtil.getId(entity) == null) {
			dataStore.create(entity);
		} else {
			dataStore.update(entity);
		}
	}

	/**
	 * Create an object.
	 *
	 * @param entity Object to create
	 */
	public final void create(final E entity) {
		dataStore.create(entity);
	}

	/**
	 * Update an object.
	 *
	 * @param entity Object to update
	 */
	public final void update(final E entity) {
		dataStore.update(entity);
	}

	/**
	 * Reloads entity from fragment, and keep fragment modifications.
	 *
	 * @param fragment  merged from datastore and input
	 * @return merged root entity merged with the fragment
	 */
	public final E reloadAndMerge(final Fragment<E> fragment) {
		final DtDefinition fragmentDefinition = DtObjectUtil.findDtDefinition(fragment);
		final DtField idField = fragmentDefinition.getFragment().get().getIdField().get();
		final P entityId = (P) idField.getDataAccessor().getValue(fragment);//etrange on utilise l'accessor du fragment sur l'entity
		final E dto = get(entityId);
		for (final DtField fragmentField : fragmentDefinition.getFields()) {
			final DataAccessor dataAccessor = fragmentField.getDataAccessor();
			dataAccessor.setValue(dto, dataAccessor.getValue(fragment)); //etrange on utilise l'accessor du fragment sur l'entity
		}
		return dto;
	}

	/**
	 * Suppression d'un objet persistant par son URI.
	 *
	 * @param uri URI de l'objet à supprimer
	 */
	public final void delete(final URI<E> uri) {
		dataStore.delete(uri);
	}

	/**
	 * Suppression d'un objet persistant par son identifiant.<br>
	 * Cette méthode est utile uniquement dans les cas où l'identifiant est un identifiant technique (ex: entier calculé
	 * via une séquence).
	 *
	 * @param id identifiant de l'objet persistant à supprimer
	 */
	public final void delete(final P id) {
		delete(createDtObjectURI(id));
	}

	/**
	 * Récupération d'un objet persistant par son URI. L'objet doit exister.
	 *
	 * @param uri URI de l'objet à récupérer
	 * @return D Object recherché
	 */
	public final E get(final URI<E> uri) {
		return dataStore.<E> read(uri);
	}

	/**
	 * Récupération d'un fragment persistant par son URI. L'objet doit exister.
	 *
	 * @param uri URI de l'objet à récupérer
	 * @param fragmentClass Fragment class
	 * @return F Fragment recherché
	 */
	public final <F extends Fragment<E>> F getFragment(final URI<E> uri, final Class<F> fragmentClass) {
		final E dto = dataStore.<E> read(uri);
		final DtDefinition fragmentDefinition = DtObjectUtil.findDtDefinition(fragmentClass);
		final F fragment = fragmentClass.cast(DtObjectUtil.createDtObject(fragmentDefinition));
		for (final DtField fragmentField : fragmentDefinition.getFields()) {
			final DataAccessor dataAccessor = fragmentField.getDataAccessor();
			dataAccessor.setValue(fragment, dataAccessor.getValue(dto)); //etrange on utilise l'accessor du fragment sur l'entity
		}
		return fragment;
	}

	/**
	 * Récupération d'un objet persistant par son identifiant.<br>
	 * Cette méthode est utile uniquement dans les cas où l'identifiant est un identifiant technique (ex: entier calculé
	 * via une séquence).
	 *
	 * @param id identifiant de l'objet persistant recherché
	 * @return D Object objet recherché
	 */
	public final E get(final P id) {
		return get(createDtObjectURI(id));
	}

	/**
	 * Récupération d'un fragment persistant par son identifiant.<br>
	 *
	 * @param id identifiant de l'objet persistant recherché
	 * @param fragmentClass Fragment class
	 * @return D Fragment recherché
	 */
	public final <F extends Fragment<E>> F get(final P id, final Class<F> fragmentClass) {
		return getFragment(new URI<E>(DtObjectUtil.findDtDefinition(fragmentClass).getFragment().get(), id), fragmentClass);
	}

	/**
	 * Retourne l'URI de DtObject correspondant à une URN de définition et une valeur d'URI donnés.
	 *
	 * @param id identifiant de l'objet persistant recherché
	 * @return URI recherchée
	 */
	protected final URI<E> createDtObjectURI(final P id) {
		return new URI<>(dtDefinition, id);
	}

	/**
	 * @param fieldName de l'object à récupérer NOT NULL
	 * @param value de l'object à récupérer NOT NULL
	 * @param maxRows Nombre maximum de ligne
	 * @return DtList<D> récupéré NOT NUL
	 */
	public final DtList<E> getListByDtField(final String fieldName, final Object value, final int maxRows) {
		final FilterCriteria<E> criteria = new FilterCriteriaBuilder<E>().addFilter(fieldName, value).build();
		// Verification de la valeur est du type du champ
		dtDefinition.getField(fieldName).getDomain().getDataType().checkValue(value);
		return dataStore.<E> findAll(new DtListURIForCriteria<>(dtDefinition, criteria, maxRows));
	}

	/**
	 * Find one and only one object matching the criteria.
	 * If there are many results or no result an exception is thrown
	 * @param criteria the filter criteria
	 * @return  the result
	 */
	public final E find(final Criteria<E> criteria) {
		return findOptional(criteria)
				.orElseThrow(() -> new NullPointerException("No data found"));
	}

	/**
	 * Find one or zero object matching the criteria.
	 * If there are many results an exception is thrown
	 * @param criteria the filter criteria
	 * @return  the optional result
	 */
	public final Optional<E> findOptional(final Criteria<E> criteria) {
		final DtList<E> list = dataStore.<E> findAll(new DtListURIForCriteria<>(dtDefinition, criteria, 2));
		Assertion.checkState(list.size() <= 1, "Too many results");
		return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}

	/**
	 * @param criteria Critére de recherche NOT NULL
	 * @param maxRows Nombre maximum de ligne
	 * @return DtList<D> récupéré NOT NUL
	 * @deprecated Use findAll instead
	 */
	@Deprecated
	public final DtList<E> getList(final Criteria<E> criteria, final int maxRows) {
		return dataStore.<E> findAll(new DtListURIForCriteria<>(dtDefinition, criteria, maxRows));
	}

	/**
	 * @param criteria Thr criteria
	 * @param maxRows Max rows
	 * @return DtList<D> result NOT NULL
	 */
	public final DtList<E> findAll(final Criteria<E> criteria, final int maxRows) {
		return dataStore.<E> findAll(new DtListURIForCriteria<>(dtDefinition, criteria, maxRows));
	}

	/** {@inheritDoc} */
	@Override
	public final void removeAllNN(final DtListURIForNNAssociation dtListURI) {
		brokerNN.removeAllNN(dtListURI);
	}

	/** {@inheritDoc} */
	@Override
	public final void removeNN(final DtListURIForNNAssociation dtListURI, final URI uriToDelete) {
		brokerNN.removeNN(dtListURI, uriToDelete);
	}

	/**
	 * Mise à jour des associations n-n.
	 *
	 * @param <FK> <FK extends DtObject>
	 * @param dtListURI DtList de référence
	 * @param newDtc DtList modifiée
	 */
	public final <FK extends Entity> void updateNN(final DtListURIForNNAssociation dtListURI, final DtList<FK> newDtc) {
		Assertion.checkNotNull(newDtc);
		//-----
		final List<URI> objectURIs = new ArrayList<>();
		for (final FK dto : newDtc) {
			objectURIs.add(DtObjectUtil.createURI(dto));
		}
		updateNN(dtListURI, objectURIs);
	}

	/** {@inheritDoc} */
	@Override
	public final void updateNN(final DtListURIForNNAssociation dtListURI, final List<URI> newUriList) {
		brokerNN.updateNN(dtListURI, newUriList);
	}

	/** {@inheritDoc} */
	@Override
	public final void appendNN(final DtListURIForNNAssociation dtListURI, final URI uriToAppend) {
		brokerNN.appendNN(dtListURI, uriToAppend);
	}

	/**
	 * Ajout un objet à la collection existante.
	 *
	 * @param dtListURI DtList de référence
	 * @param entity the entity to append
	 */
	public final void appendNN(final DtListURIForNNAssociation dtListURI, final Entity entity) {
		brokerNN.appendNN(dtListURI, entity.getURI());
	}
}