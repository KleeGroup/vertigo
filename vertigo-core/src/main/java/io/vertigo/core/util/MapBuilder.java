/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;

/**
 * The MapBuilder class allows to build a map.
 * The map can be immutable using unmodifiable().
 * Several put() methods exist to cover the frequent cases.
 *
 * @author pchretien
 * @param <K> the type of keys
 * @param <V> the type of mapped values
 */
public final class MapBuilder<K, V> implements Builder<Map<K, V>> {
	private final Map<K, V> myMap = new HashMap<>();
	private boolean unmodifiable;

	/**
	 * Adds key-value.
	 * If the same value exists then an exception is thrown.
	 *
	 * @param key Key
	 * @param value Value not null
	 * @return this builder
	 */
	public MapBuilder<K, V> putCheckKeyNotExists(final K key, final V value) {
		Assertion.check()
				.isNotNull(key)
				.isNotNull(value);
		//-----
		final Object previous = myMap.put(key, value);
		Assertion.check()
				.isNull(previous , "Data with key '{0}' already registered", key);
		return this;
	}

	/**
	 * Adds a map of key-value.
	 * Values are required.
	 * @param map Map
	 * @return this builder
	 */
	public MapBuilder<K, V> putAll(final Map<K, V> map) {
		Assertion.check()
				.isNotNull(map);
		//-----
		map.forEach(this::put);
		return this;
	}

	/**
	 * Adds key-value.
	 * The value is required.
	 * @param key Key
	 * @param value Value not null
	 * @return this builder
	 */
	public MapBuilder<K, V> put(final K key, final V value) {
		Assertion.check()
				.isNotNull(key)
				.isNotNull(value);
		//-----
		myMap.put(key, value);
		return this;
	}

	/**
	 * Adds nullable key-value.
	 * @param key Key
	 * @param value Value nullable
	 * @return this builder
	 */
	public MapBuilder<K, V> putNullable(final K key, final V value) {
		Assertion.check()
				.isNotNull(key);
		//-----
		if (value != null) {
			myMap.put(key, value);
		}
		return this;
	}

	/**
	 * Makes this map as unmodifiable.
	 * @return this builder
	 */
	public MapBuilder<K, V> unmodifiable() {
		unmodifiable = true;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Map<K, V> build() {
		return unmodifiable ? Collections.unmodifiableMap(myMap) : myMap;
	}
}
