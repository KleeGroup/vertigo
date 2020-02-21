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

/**
 * Keep value, with it's createTime.
 * @author npiedeloup
 */
final class DelayedMemoryCacheValue {
	private final long createTime;
	private final Object value;

	/**
	 * Constructor.
	 * @param value Value
	 */
	DelayedMemoryCacheValue(final Object value) {
		this.value = value;
		createTime = System.currentTimeMillis();
	}

	/**
	 * @return Value
	 */
	Object getValue() {
		return value;
	}

	/**
	 * @return Creation time
	 */
	long getCreateTime() {
		return createTime;
	}
}
