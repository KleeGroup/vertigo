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
package io.vertigo.dynamo.plugins.kvstore.delayedmemory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonDefinition;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.core.param.ParamValue;
import io.vertigo.dynamo.impl.kvstore.KVStorePlugin;
import io.vertigo.lang.Assertion;

/**
 * Memory implementation of UiSecurityTokenCachePlugin.
 * This store ISN'T transactional !!
 * Purge is garantee by Timer every minute.
 *
 * @author pchretien, npiedeloup
 */
public final class DelayedMemoryKVStorePlugin implements KVStorePlugin, SimpleDefinitionProvider {

	private static final Logger LOGGER = LogManager.getLogger(DelayedMemoryKVStorePlugin.class);
	private final List<String> collections;

	private final int timeToLiveSeconds;
	private final DelayQueue<DelayedMemoryKey> timeoutQueue = new DelayQueue<>();

	private final Map<String, Map<String, DelayedMemoryCacheValue>> collectionsData = new HashMap<>();

	/**
	 * Constructor.
	 * @param collections List of collections managed by this plugin (comma separated)
	 * @param daemonManager Manager des daemons
	 * @param timeToLiveSeconds life time of elements (seconde)
	 */
	@Inject
	public DelayedMemoryKVStorePlugin(
			final @ParamValue("collections") String collections,
			final DaemonManager daemonManager,
			final @ParamValue("timeToLiveSeconds") int timeToLiveSeconds) {
		Assertion.checkArgNotEmpty(collections);
		//-----
		this.collections = Arrays.stream(collections.split(", "))
				.map(String::trim)
				.peek(collection -> collectionsData.put(collection, new ConcurrentHashMap<String, DelayedMemoryCacheValue>()))
				.collect(Collectors.toList());
		//-----
		this.timeToLiveSeconds = timeToLiveSeconds;
	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		final int purgePeriod = Math.min(1 * 60, timeToLiveSeconds);
		return Collections.singletonList(new DaemonDefinition("DmnKvDataStoreCache", () -> new RemoveTooOldElementsDaemon(this), purgePeriod));
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getCollections() {
		return collections;
	}

	private Map<String, DelayedMemoryCacheValue> getCollectionData(final String collection) {
		final Map<String, DelayedMemoryCacheValue> collectionData = collectionsData.get(collection);
		Assertion.checkNotNull(collectionData, "collection {0} is null", collection);
		return collectionData;
	}

	/** {@inheritDoc} */
	@Override
	public int count(final String collection) {
		return getCollectionData(collection).size();
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String collection, final String key, final Object element) {
		Assertion.checkArgNotEmpty(collection);
		Assertion.checkArgNotEmpty(key);
		Assertion.checkNotNull(element);
		//-----
		final DelayedMemoryCacheValue cacheValue = new DelayedMemoryCacheValue(element);
		getCollectionData(collection).put(key, cacheValue);
		timeoutQueue.put(new DelayedMemoryKey(collection, key, cacheValue.getCreateTime() + timeToLiveSeconds * 1000));
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final String collection, final String key) {
		Assertion.checkArgNotEmpty(collection);
		Assertion.checkArgNotEmpty(key);
		//-----
		getCollectionData(collection).remove(key);
	}

	/** {@inheritDoc} */
	@Override
	public void clear(final String collection) {
		Assertion.checkArgNotEmpty(collection);
		//-----
		getCollectionData(collection).clear();
	}

	/** {@inheritDoc} */
	@Override
	public <C> Optional<C> find(final String collection, final String key, final Class<C> clazz) {
		Assertion.checkArgNotEmpty(collection);
		Assertion.checkArgNotEmpty(key);
		Assertion.checkNotNull(clazz);
		//-----
		final DelayedMemoryCacheValue cacheValue = getCollectionData(collection).get(key);
		if (cacheValue != null && !isTooOld(cacheValue)) {
			return Optional.of(clazz.cast(cacheValue.getValue()));
		}
		getCollectionData(collection).remove(key);
		return Optional.empty(); //key expired : return null
	}

	/** {@inheritDoc} */
	@Override
	public <C> List<C> findAll(final String collection, final int skip, final Integer limit, final Class<C> clazz) {
		throw new UnsupportedOperationException("This implementation doesn't use ordered datas. Method findAll can't be called.");
	}

	/**
	 * Purge les elements trop vieux.
	 */
	void removeTooOldElements() {
		final int maxChecked = 500;
		int checked = 0;
		//Les elements sont parcouru dans l'ordre d'insertion (sans lock)
		while (checked < maxChecked) {
			final DelayedMemoryKey delayedKey = timeoutQueue.poll();
			if (delayedKey != null) {
				getCollectionData(delayedKey.getCollection()).remove(delayedKey.getKey());
				checked++;
			} else {
				break;
			}
		}
		LOGGER.info("purge {} elements", checked);
	}

	private boolean isTooOld(final DelayedMemoryCacheValue cacheValue) {
		return System.currentTimeMillis() - cacheValue.getCreateTime() >= timeToLiveSeconds * 1000;
	}

	/**
	 *
	 * @author npiedeloup
	 */
	public static final class RemoveTooOldElementsDaemon implements Daemon {
		private final DelayedMemoryKVStorePlugin delayedMemoryKVDataStorePlugin;

		/**
		 * @param delayedMemoryKVDataStorePlugin This plugin
		 */
		public RemoveTooOldElementsDaemon(final DelayedMemoryKVStorePlugin delayedMemoryKVDataStorePlugin) {
			Assertion.checkNotNull(delayedMemoryKVDataStorePlugin);
			//------
			this.delayedMemoryKVDataStorePlugin = delayedMemoryKVDataStorePlugin;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			delayedMemoryKVDataStorePlugin.removeTooOldElements();
		}
	}

}
