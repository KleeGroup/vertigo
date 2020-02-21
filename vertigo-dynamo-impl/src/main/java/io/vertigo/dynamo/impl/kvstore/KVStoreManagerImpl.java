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
package io.vertigo.dynamo.impl.kvstore;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.dynamo.kvstore.KVStoreManager;
import io.vertigo.lang.Assertion;
import io.vertigo.util.MapBuilder;

/**
* Standard implementation of the Key-Value DataBase.
*
* @author pchretien
*/
public final class KVStoreManagerImpl implements KVStoreManager {
	private final Map<String, KVStorePlugin> kvStoreByCollection;

	/**
	 * Constructor.
	 * @param kvStorePlugins kvStore list
	 */
	@Inject
	public KVStoreManagerImpl(final List<KVStorePlugin> kvStorePlugins) {
		Assertion.checkNotNull(kvStorePlugins);
		//-----
		final MapBuilder<String, KVStorePlugin> mapBuilder = new MapBuilder<>();
		for (final KVStorePlugin kvDataStorePlugin : kvStorePlugins) {
			for (final String collection : kvDataStorePlugin.getCollections()) {
				mapBuilder.putCheckKeyNotExists(collection, kvDataStorePlugin);
			}
		}
		kvStoreByCollection = mapBuilder.unmodifiable().build();
	}

	private KVStorePlugin getKVStorePlugin(final String collection) {
		Assertion.checkArgNotEmpty(collection);
		//-----
		final KVStorePlugin kvStorePlugin = kvStoreByCollection.get(collection);
		Assertion.checkNotNull(kvStorePlugin, "no store found for collection '{0}'", collection);
		return kvStorePlugin;
	}

	/** {@inheritDoc} */
	@Override
	public int count(final String collection) {
		return getKVStorePlugin(collection).count(collection);
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String collection, final String id, final Object element) {
		getKVStorePlugin(collection).put(collection, id, element);
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final String collection, final String id) {
		getKVStorePlugin(collection).remove(collection, id);
	}

	/** {@inheritDoc} */
	@Override
	public void clear(final String collection) {
		getKVStorePlugin(collection).clear(collection);
	}

	/** {@inheritDoc} */
	@Override
	public <C> Optional<C> find(final String collection, final String id, final Class<C> clazz) {
		return getKVStorePlugin(collection).find(collection, id, clazz);
	}

	/** {@inheritDoc} */
	@Override
	public <C> List<C> findAll(final String collection, final int skip, final Integer limit, final Class<C> clazz) {
		return getKVStorePlugin(collection).findAll(collection, skip, limit, clazz);
	}

}
