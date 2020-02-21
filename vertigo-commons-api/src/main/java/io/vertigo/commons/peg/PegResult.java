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
package io.vertigo.commons.peg;

import io.vertigo.lang.Assertion;

/**
 * 	Contains the result of a parsing operation.
 *   - the new index position
 *   - the object created
 * @author pchretien
 *
 * @param <R> the type of the Result Object
 */
public final class PegResult<R> {
	private final int index;
	private final R value;

	/**
	 * Constructor.
	 * @param index
	 * @param result
	 */
	PegResult(final int index, final R result) {
		Assertion.checkNotNull(result);
		//---
		this.index = index;
		this.value = result;
	}

	/**
	 * @return Index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the result value
	 */
	public R getValue() {
		return value;
	}

}
