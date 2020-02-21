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
package io.vertigo.lang;

import java.util.Objects;

/**
 * Tuples are immutable objects.
 * Tuple with 2 Objects.
 * @param <A> Type one
 * @param <B> Type two
 *
 * @author pchretien
 */
public final class Tuple<A, B> {

	/**
	 * Creates a Tuple with 2 objects
	 * @param val1 the first object
	 * @param val2 the second object
	 * @return the new tuple created
	 */
	public static <A, B> Tuple<A, B> of(final A val1, final B val2) {
		return new Tuple<>(val1, val2);
	}

	private final A val1;
	private final B val2;

	/**
	* Creates a new instance of Tuple.
	*
	* @param val1 Value 1.
	* @param val2 Value 2.
	*/
	private Tuple(final A val1, final B val2) {
		this.val1 = val1;
		this.val2 = val2;
	}

	/**
	* @return  Value#1.
	*/
	public A getVal1() {
		return val1;
	}

	/**
	* @return  Value#2.
	 */
	public B getVal2() {
		return val2;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(val1, val2);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		if (object instanceof Tuple) {
			final Tuple<?, ?> that = Tuple.class.cast(object);
			return Objects.equals(val1, that.val1) && Objects.equals(val2, that.val2);
		}
		return false;
	}
}
