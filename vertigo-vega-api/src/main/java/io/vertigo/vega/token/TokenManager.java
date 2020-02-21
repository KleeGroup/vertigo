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
package io.vertigo.vega.token;

import java.io.Serializable;
import java.util.Optional;

import io.vertigo.core.component.Manager;

/**
 * Manager of Security Access Token.
 * @author npiedeloup (16 juil. 2014 12:49:49)
 */
public interface TokenManager extends Manager {

	/**
	 * Store object and return unique key.
	 * Same object can be put multiple times, always return a new unique key.
	 * @param data Object to store
	 * @return unique key of this object
	 */
	<D extends Serializable> String put(D data);

	/**
	 * Get object by key.
	 * @param key key of this object
	 * @return Object store
	 */
	<D extends Serializable> Optional<D> get(String key);

	/**
	 * Get and remove object by key.
	 * @param key key of this object
	 * @return Object store or null if unknown
	 */
	<D extends Serializable> Optional<D> getAndRemove(String key);

}
