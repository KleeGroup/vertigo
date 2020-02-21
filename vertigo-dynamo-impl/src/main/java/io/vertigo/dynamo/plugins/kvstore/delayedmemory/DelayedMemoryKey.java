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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import io.vertigo.lang.Assertion;

/**
 * Delayed key of SecurityToken.
 * @author npiedeloup (15 juil. 2014 17:56:15)
 */
final class DelayedMemoryKey implements Delayed {
	private final long timeoutTime;
	private final String collection;
	private final String key;

	/**
	 * Constructor.
	 * @param collection Collection of this element
	 * @param key Security Token key
	 * @param timeoutTime When key expired
	 */
	public DelayedMemoryKey(final String collection, final String key, final long timeoutTime) {
		this.collection = collection;
		this.key = key;
		this.timeoutTime = timeoutTime;
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(final Delayed o) {
		Assertion.checkArgument(o instanceof DelayedMemoryKey, "Only DelayedKey is supported ({0})", o.getClass());
		//-----
		return (int) (timeoutTime - ((DelayedMemoryKey) o).timeoutTime);
	}

	/** {@inheritDoc} */
	@Override
	public long getDelay(final TimeUnit unit) {
		return unit.convert(timeoutTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * @return Collection
	 */
	public String getCollection() {
		return collection;
	}

	/**
	 * @return Security Token key
	 */
	public String getKey() {
		return key;
	}
}
