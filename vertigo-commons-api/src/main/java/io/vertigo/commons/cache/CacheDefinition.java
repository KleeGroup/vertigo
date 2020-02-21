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
/**
 * vertigo - simple
 java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons.cache;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.core.definition.DefinitionUtil;

/**
 * The cache definition defines the configuration of a cache.
 * This definition
 *  - has a name;
 *  - and 3 params that define the strategy of caching
 *    - max elements in memory
 *    - time to live
 *    - time to idle
 *
 * @author pchretien
 */
@DefinitionPrefix("Cache")
public final class CacheDefinition implements Definition {
	private final String name;
	private final boolean serializeElements;
	private final int maxElementsInMemory;
	private final int timeToLiveSeconds;
	private final int timeToIdleSeconds;
	private final boolean isReloadedByList;

	/**
	 * Constructor.
	 * @param name the name of the cache
	 * @param serializeElements If elements are serialized
	 * @param maxElementsInMemory Max elements stored in memory
	 * @param timeToLiveSeconds Time to live (in seconds)
	 * @param timeToIdleSeconds Time to live when idle (in seconds)
	 */
	public CacheDefinition(
			final String name,
			final boolean serializeElements,
			final int maxElementsInMemory,
			final int timeToLiveSeconds,
			final int timeToIdleSeconds,
			final boolean isReloadedByList) {
		DefinitionUtil.checkName(name, CacheDefinition.class);
		//-----
		this.name = name;
		this.serializeElements = serializeElements;
		this.maxElementsInMemory = maxElementsInMemory;
		this.timeToLiveSeconds = timeToLiveSeconds;
		this.timeToIdleSeconds = timeToIdleSeconds;
		this.isReloadedByList = isReloadedByList;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return elements should be serialized
	 */
	public boolean shouldSerializeElements() {
		return serializeElements;
	}

	/**
	 * @return Max elements stored in memory
	 */
	public int getMaxElementsInMemory() {
		return maxElementsInMemory;
	}

	/**
	 * @return Time to live (in seconds)
	 */
	public int getTimeToLiveSeconds() {
		return timeToLiveSeconds;
	}

	/**
	 * @return Time tio idle (in seconds)
	 */
	public int getTimeToIdleSeconds() {
		return timeToIdleSeconds;
	}

	public boolean isReloadedByList() {
		return isReloadedByList;
	}
}
