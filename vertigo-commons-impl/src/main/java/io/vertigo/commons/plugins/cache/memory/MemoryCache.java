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
package io.vertigo.commons.plugins.cache.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import io.vertigo.lang.Assertion;

/**
 * Implementation d'un cache memoire.
 * La purge des éléments trop vieux se fait lors de la lecture.
 *
 * @author npiedeloup
 */
final class MemoryCache {
	private final String name;
	private final int timeToLiveSeconds;
	private long totalHits;
	private long totalCalls;
	private final Map<Serializable, MemoryCacheValue> cacheDatas = new HashMap<>();

	/**
	 * Constructor.
	 * @param name Nom du cache
	 * @param timeToLiveSeconds Durée de vie en secondes
	 */
	MemoryCache(final String name, final int timeToLiveSeconds) {
		Assertion.checkArgNotEmpty(name);
		//-----
		this.name = name;
		this.timeToLiveSeconds = timeToLiveSeconds;
	}

	/**
	 * @return Conf : Nom du cache
	 */
	String getName() {
		return name;
	}

	/**
	 * @return Conf : temps de vie en seconde des éléments
	 */
	int getTimeToLiveSeconds() {
		return timeToLiveSeconds;
	}

	/**
	 * @return Nombre d'élément en cache
	 */
	synchronized int getElementCount() {
		return cacheDatas.size();
	}

	/**
	 * @return Nombre de cache hit
	 */
	synchronized long getHits() {
		return totalHits;
	}

	/**
	 * @return Nombre d'appel au cache
	 */
	synchronized long getCalls() {
		return totalCalls;
	}

	/**
	 * @param key Clé de l'élément
	 * @return élément du cache, null si n'existe pas ou obsolete
	 */
	synchronized Object get(final Serializable key) {
		totalCalls++;
		final MemoryCacheValue cacheValue = cacheDatas.get(key);
		if (cacheValue != null) {
			if (isAlive(cacheValue)) {
				totalHits++;
				return cacheValue.getValue();
			}
			cacheDatas.remove(key);
		}
		return null;
	}

	private boolean isAlive(final MemoryCacheValue cacheValue) {
		//Data is alive
		// - if its age is less than than 'timeToLiveSeconds'
		return (System.currentTimeMillis() - cacheValue.getCreateTime()) < timeToLiveSeconds * 1000L;
	}

	/**
	 * @param key Clé de l'élément
	 * @param value Element
	 */
	synchronized void put(final Serializable key, final Object value) {
		cacheDatas.put(key, new MemoryCacheValue(value));
	}

	/**
	 * Vide le cache.
	 */
	synchronized void removeAll() {
		cacheDatas.clear();
	}

	/**
	 * @param key Clé de l'élément à supprimer
	 * @return si supprimé
	 */
	synchronized boolean remove(final Serializable key) {
		return cacheDatas.remove(key) != null;
	}
}
