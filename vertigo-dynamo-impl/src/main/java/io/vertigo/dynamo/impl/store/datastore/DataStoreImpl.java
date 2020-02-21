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
package io.vertigo.dynamo.impl.store.datastore;

import java.util.List;

import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.store.datastore.cache.CacheDataStore;
import io.vertigo.dynamo.impl.store.datastore.logical.LogicalDataStoreConfig;
import io.vertigo.dynamo.store.StoreEvent;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.datastore.BrokerNN;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.lang.Assertion;

/**
 * Implementation of DataStore.
 * @author pchretien
 */
public final class DataStoreImpl implements DataStore, SimpleDefinitionProvider {
	private static final Criteria CRITERIA_ALWAYS_TRUE = Criterions.alwaysTrue();

	/** Le store est le point d'accès unique à la base (sql, xml, fichier plat...). */
	private final CacheDataStore cacheDataStore;
	private final LogicalDataStoreConfig logicalStoreConfig;
	private final EventBusManager eventBusManager;
	private final VTransactionManager transactionManager;

	private final BrokerNNImpl brokerNN;

	/**
	 * Constructor
	 * @param collectionsManager collectionsManager
	 * @param storeManager storeManager
	 * @param transactionManager transactionManager
	 * @param eventBusManager eventBusManager
	 * @param dataStoreConfig dataStoreConfig
	 */
	public DataStoreImpl(
			final CollectionsManager collectionsManager,
			final StoreManager storeManager,
			final VTransactionManager transactionManager,
			final EventBusManager eventBusManager,
			final TaskManager taskManager,
			final DataStoreConfigImpl dataStoreConfig) {
		Assertion.checkNotNull(collectionsManager);
		Assertion.checkNotNull(storeManager);
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(eventBusManager);
		Assertion.checkNotNull(taskManager);
		Assertion.checkNotNull(dataStoreConfig);
		//-----
		logicalStoreConfig = dataStoreConfig.getLogicalStoreConfig();
		cacheDataStore = new CacheDataStore(collectionsManager, storeManager, dataStoreConfig);
		this.eventBusManager = eventBusManager;
		this.transactionManager = transactionManager;
		brokerNN = new BrokerNNImpl(taskManager);
	}

	private DataStorePlugin getPhysicalStore(final DtDefinition dtDefinition) {
		return logicalStoreConfig.getPhysicalDataStore(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> E readOneForUpdate(final UID<E> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtDefinition dtDefinition = uri.getDefinition();
		final E entity = getPhysicalStore(dtDefinition).readNullableForUpdate(dtDefinition, uri);
		//-----
		Assertion.checkNotNull(entity, "no entity found for : '{0}'", uri);
		//-----
		fireAfterCommit(StoreEvent.Type.UPDATE, uri);
		return entity;
	}

	private void fireAfterCommit(final StoreEvent.Type evenType, final UID<?> uri) {
		transactionManager.getCurrentTransaction().addAfterCompletion(
				(final boolean txCommitted) -> {
					if (txCommitted) {//send event only is tx successful
						eventBusManager.post(new StoreEvent(evenType, uri));
					}
				});
	}

	//--- Transactionnal Event

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> E create(final E entity) {
		Assertion.checkNotNull(entity);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(entity);
		final E createdEntity = getPhysicalStore(dtDefinition).create(dtDefinition, entity);
		//-----
		fireAfterCommit(StoreEvent.Type.CREATE, UID.of(dtDefinition, DtObjectUtil.getId(createdEntity)));
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
		return createdEntity;
	}

	/** {@inheritDoc} */
	@Override
	public void update(final Entity entity) {
		Assertion.checkNotNull(entity);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(entity);
		getPhysicalStore(dtDefinition).update(dtDefinition, entity);
		//-----
		fireAfterCommit(StoreEvent.Type.UPDATE, UID.of(dtDefinition, DtObjectUtil.getId(entity)));
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final UID<? extends Entity> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtDefinition dtDefinition = uri.getDefinition();
		getPhysicalStore(dtDefinition).delete(dtDefinition, uri);
		//-----
		fireAfterCommit(StoreEvent.Type.DELETE, uri);
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> E readOne(final UID<E> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final E entity = cacheDataStore.readNullable(uri);
		//-----
		Assertion.checkNotNull(entity, "no entity found for : '{0}'", uri);
		return entity;
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> DtList<E> findAll(final DtListURI uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtList<E> list = cacheDataStore.findAll(uri);
		//-----
		Assertion.checkNotNull(list);
		return list;
	}

	/** {@inheritDoc} */
	@Override
	public int count(final DtDefinition dtDefinition) {
		return getPhysicalStore(dtDefinition).count(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> DtList<E> find(final DtDefinition dtDefinition, final Criteria<E> criteria, final DtListState dtListState) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(dtListState);
		//-----
		final DtList<E> list = cacheDataStore.findByCriteria(dtDefinition, criteria != null ? criteria : CRITERIA_ALWAYS_TRUE, dtListState);
		//-----
		Assertion.checkNotNull(list);
		return list;

	}

	/** {@inheritDoc} */
	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return cacheDataStore.provideDefinitions(definitionSpace);
	}

	//------

	/** {@inheritDoc} */
	@Override
	public BrokerNN getBrokerNN() {
		return brokerNN;
	}

}
