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
package io.vertigo.dynamo.impl.store;

import java.util.List;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.commons.cache.CacheDefinition;
import io.vertigo.commons.cache.CacheManager;
import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.component.Activeable;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.impl.store.datastore.DataStoreConfigImpl;
import io.vertigo.dynamo.impl.store.datastore.DataStoreImpl;
import io.vertigo.dynamo.impl.store.datastore.DataStorePlugin;
import io.vertigo.dynamo.impl.store.datastore.MasterDataConfigImpl;
import io.vertigo.dynamo.impl.store.datastore.cache.CacheData;
import io.vertigo.dynamo.impl.store.filestore.FileStoreConfig;
import io.vertigo.dynamo.impl.store.filestore.FileStoreImpl;
import io.vertigo.dynamo.impl.store.filestore.FileStorePlugin;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.dynamo.store.datastore.DataStoreConfig;
import io.vertigo.dynamo.store.datastore.MasterDataConfig;
import io.vertigo.dynamo.store.datastore.MasterDataDefinition;
import io.vertigo.dynamo.store.filestore.FileStore;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.lang.Assertion;

/**
* Implémentation standard du gestionnaire des données et des accès aux données.
*
* @author pchretien
*/
public final class StoreManagerImpl implements StoreManager, Activeable, SimpleDefinitionProvider {
	private final MasterDataConfig masterDataConfig;
	private final DataStoreConfigImpl dataStoreConfig;

	/** DataStore des objets métier et des listes. */
	private final DataStore dataStore;
	private final FileStore fileStore;

	/**
	 * Constructor.
	 * @param cacheManager cacheManager
	 * @param transactionManager transactionManager
	 * @param collectionsManager collectionsManager
	 * @param fileStorePlugins fileStorePlugins
	 * @param dataStorePlugins dataStorePlugins
	 * @param eventBusManager eventBusManager
	 */
	@Inject
	public StoreManagerImpl(
			final CacheManager cacheManager,
			final VTransactionManager transactionManager,
			final CollectionsManager collectionsManager,
			final List<FileStorePlugin> fileStorePlugins,
			final List<DataStorePlugin> dataStorePlugins,
			final EventBusManager eventBusManager,
			final TaskManager taskManager) {
		Assertion.checkNotNull(cacheManager);
		Assertion.checkNotNull(collectionsManager);
		Assertion.checkNotNull(dataStorePlugins);
		Assertion.checkNotNull(fileStorePlugins);
		Assertion.checkNotNull(eventBusManager);
		Assertion.checkNotNull(taskManager);
		//-----
		masterDataConfig = new MasterDataConfigImpl();
		//---
		//On enregistre le plugin principal du broker
		dataStoreConfig = new DataStoreConfigImpl(dataStorePlugins, cacheManager);
		dataStore = new DataStoreImpl(collectionsManager, this, transactionManager, eventBusManager, taskManager, dataStoreConfig);
		//-----
		final FileStoreConfig fileStoreConfig = new FileStoreConfig(fileStorePlugins);
		fileStore = new FileStoreImpl(fileStoreConfig);
	}

	@Override
	public void start() {
		// register as cacheable the dtDefinitions that are persistant and have a corresponding CacheDefinition
		Home.getApp().getDefinitionSpace().getAll(DtDefinition.class).stream()
				.filter(DtDefinition::isPersistent)
				.filter(dtDefinition -> Home.getApp().getDefinitionSpace().contains(CacheData.getContext(dtDefinition)))
				.forEach(dtDefinition -> dataStoreConfig.getCacheStoreConfig().registerCacheable(dtDefinition, Home.getApp().getDefinitionSpace().resolve(CacheData.getContext(dtDefinition), CacheDefinition.class).isReloadedByList()));

		Home.getApp().getDefinitionSpace().getAll(MasterDataDefinition.class)
				.forEach(masterDataConfig::register);

	}

	@Override
	public void stop() {
		// nothing

	}

	/** {@inheritDoc} */
	@Override
	public MasterDataConfig getMasterDataConfig() {
		return masterDataConfig;
	}

	/**
	 * @return Configuration du StoreManager
	 */
	@Override
	public DataStoreConfig getDataStoreConfig() {
		return dataStoreConfig;
	}

	/** {@inheritDoc} */
	@Override
	public DataStore getDataStore() {
		return dataStore;
	}

	/** {@inheritDoc} */
	@Override
	public FileStore getFileStore() {
		return fileStore;
	}

	/** {@inheritDoc} */
	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return ((SimpleDefinitionProvider) dataStore).provideDefinitions(definitionSpace);
	}
}
