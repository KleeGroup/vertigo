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
package io.vertigo.dynamo.impl.store.datastore.logical;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.impl.store.datastore.DataStorePlugin;
import io.vertigo.lang.Assertion;

/**
 * This class defines how the dataSpaces are mapped to the physical stores.
 * A dataSpace is a set of collections.
 * A dataSpace has a name.
 * A dataSpace can have one dataStore.
 *
 * @author pchretien, npiedeloup
 */
public final class LogicalDataStoreConfig {
	/**
	 * Map (collection-dataStorePlugin).
	 * This map defines the dataStore for each collection */
	private final Map<String, DataStorePlugin> dataStorePluginsMap;

	/**
	 * Constructor.
	 * @param dataStorePlugins DataStore plugins
	 */
	public LogicalDataStoreConfig(final List<DataStorePlugin> dataStorePlugins) {
		Assertion.checkNotNull(dataStorePlugins);
		//-----
		final Map<String, DataStorePlugin> pluginsMap = new HashMap<>();
		for (final DataStorePlugin dataStorePlugin : dataStorePlugins) {
			final String dataSpace = dataStorePlugin.getDataSpace();
			final DataStorePlugin previous = pluginsMap.put(dataSpace, dataStorePlugin);
			Assertion.checkState(previous == null, "this dataSpace {0} is already registered", dataSpace);
		}
		dataStorePluginsMap = Collections.unmodifiableMap(pluginsMap);
	}

	/**
	 * Provides a 'DataStorePlugin' for the specified 'DtDefinition'.
	 * Each DtDefinition is mapped to a collection.
	 * @param dtDefinition the DtDefinition
	 * @return the dataStore used for the specified 'DtDefinition'
	 */
	public DataStorePlugin getPhysicalDataStore(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		return getDataStorePlugin(dtDefinition.getDataSpace());
	}

	/**
	 * Provides the name of the connection.
	 * @param dataSpace the dataSpace
	 * @return the name of the connection
	 */
	public String getConnectionName(final String dataSpace) {
		Assertion.checkArgNotEmpty(dataSpace);
		//-----
		return getDataStorePlugin(dataSpace).getConnectionName();
	}

	private DataStorePlugin getDataStorePlugin(final String dataSpace) {
		Assertion.checkArgNotEmpty(dataSpace);
		//-----
		final DataStorePlugin dataStore = dataStorePluginsMap.get(dataSpace);
		Assertion.checkNotNull(dataStore, "No store found mapped to collection '{0}'", dataSpace);
		return dataStore;
	}
}
