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
package io.vertigo.dynamo.plugins.kvstore.berkeley;

/**
 * Collection configuration.
 * @author npiedeloup
 */
final class BerkeleyCollectionConfig {

	private final String collectionName;
	private final long timeToLiveSeconds;
	private final boolean inMemory;

	/**
	 * Constructor.
	 * @param collectionName Collection name
	 * @param timeToLiveSeconds Elements time to live in second
	 * @param inMemory Collection store in memory
	 */
	BerkeleyCollectionConfig(final String collectionName, final long timeToLiveSeconds, final boolean inMemory) {
		this.collectionName = collectionName;
		this.timeToLiveSeconds = timeToLiveSeconds;
		this.inMemory = inMemory;
	}

	/**
	 * @return collectionName
	 */
	String getCollectionName() {
		return collectionName;
	}

	/**
	 * @return timeToLiveSeconds
	 */
	long getTimeToLiveSeconds() {
		return timeToLiveSeconds;
	}

	/**
	 * @return inMemory
	 */
	boolean isInMemory() {
		return inMemory;
	}
}
