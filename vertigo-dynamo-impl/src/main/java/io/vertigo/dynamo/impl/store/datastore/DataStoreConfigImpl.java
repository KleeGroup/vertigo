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

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.dynamo.impl.store.datastore.cache.CacheDataStoreConfig;
import io.vertigo.dynamo.impl.store.datastore.logical.LogicalDataStoreConfig;
import io.vertigo.dynamo.store.datastore.DataStoreConfig;
import io.vertigo.lang.Assertion;

/**
 * Implémentation Standard du StoreProvider.
 *
 * @author pchretien
 */
public final class DataStoreConfigImpl implements DataStoreConfig {
	private final CacheDataStoreConfig cacheStoreConfig;
	private final LogicalDataStoreConfig logicalDataStoreConfig;

	/**
	 * Constructor.
	 * @param dataStorePlugins DataStorePlugins list
	 * @param cacheManager Manager de gestion du cache
	 */
	public DataStoreConfigImpl(final List<DataStorePlugin> dataStorePlugins, final CacheManager cacheManager) {
		Assertion.checkNotNull(dataStorePlugins);
		Assertion.checkNotNull(cacheManager);
		//-----
		cacheStoreConfig = new CacheDataStoreConfig(cacheManager);
		logicalDataStoreConfig = new LogicalDataStoreConfig(dataStorePlugins);
	}

	/**
	 * @return Data store config
	 */
	public CacheDataStoreConfig getCacheStoreConfig() {
		return cacheStoreConfig;
	}

	/**
	 * @return logical data store config
	 */
	public LogicalDataStoreConfig getLogicalStoreConfig() {
		return logicalDataStoreConfig;
	}

	/** {@inheritDoc} */
	@Override
	public String getConnectionName(final String dataSpace) {
		return logicalDataStoreConfig.getConnectionName(dataSpace);
	}
}
