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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;

/**
 * The ListBuilder class allows to build a list.
 * @author pchretien
 *
 * @param <X> the type of elements in the list
 */
public final class ListBuilder<X> implements Builder<List<X>> {
	private final List<X> list = new ArrayList<>();
	private boolean unmodifiable;
	private Comparator<? super X> myComparator;

	/**
	 * Adds a value in the list.
	 * The value CAN NOT be null.
	 * @param value Value not null
	 * @return this builder
	 */
	public ListBuilder<X> add(final X value) {
		Assertion.check()
				.isNotNull(value);
		//-----
		list.add(value);
		return this;
	}

	/**
	 * Adds a collection of values in the list.
	 * These values CAN NOT be null.
	 * @param values Values not null
	 * @return this builder
	 */
	public ListBuilder<X> addAll(final Collection<? extends X> values) {
		Assertion.check()
				.isNotNull(values);
		//-----
		values.forEach(this::add);
		return this;
	}

	/**
	 * Makes this list as unmodifiable.
	 * @return this builder
	 */
	public ListBuilder<X> unmodifiable() {
		this.unmodifiable = true;
		return this;
	}

	/**
	 * Sorts the list with the provided comparator.
	 * @param comparator the comparator to use
	 * @return this builder
	 */
	public ListBuilder<X> sort(final Comparator<? super X> comparator) {
		Assertion.check()
				.isNotNull(comparator)
				.isNull(myComparator , "comparator already set");
		//---
		myComparator = comparator;
		return this;
	}

	@Override
	public List<X> build() {
		if (myComparator != null) {
			list.sort(myComparator);
		}
		return unmodifiable ? Collections.unmodifiableList(list) : list;
	}
}
