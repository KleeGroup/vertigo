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
package io.vertigo.dynamo.impl.collections;

import java.util.Collection;
import java.util.function.UnaryOperator;

import io.vertigo.dynamo.collections.IndexDtListFunctionBuilder;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.impl.collections.functions.index.IndexFilterFunction;
import io.vertigo.lang.Assertion;

/**
 * Builder for DtListFunction powered by index engine. *
 * @param <D> List element's type
 */
final class IndexDtListFunctionBuilderImpl<D extends DtObject> implements IndexDtListFunctionBuilder<D> {

	private final IndexFilterFunction<D> indexFilterFunction;

	/**
	 * Constructor.
	 * @param indexPlugin Index plugin
	 */
	IndexDtListFunctionBuilderImpl(final IndexPlugin indexPlugin) {
		Assertion.checkNotNull(indexPlugin);
		//-----
		indexFilterFunction = new IndexFilterFunction<>(indexPlugin);
	}

	/** {@inheritDoc} */
	@Override
	public IndexDtListFunctionBuilder<D> filter(final String userKeywords, final int maxRows, final Collection<DtField> keywordsSearchedFields) {
		indexFilterFunction.filter(userKeywords, maxRows, keywordsSearchedFields);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public IndexDtListFunctionBuilder<D> sort(final String fieldName, final boolean desc) {
		indexFilterFunction.sort(fieldName, desc);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public IndexDtListFunctionBuilder<D> filterByValue(final String fieldName, final String value) {
		Assertion.checkNotNull(value);
		//-----
		final String escapedValue = value.replace("\"", "\\\"");
		final ListFilter listFilter = ListFilter.of(fieldName + ":\"" + escapedValue + "\"");
		indexFilterFunction.filter(listFilter);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public IndexDtListFunctionBuilder<D> filter(final ListFilter listFilter) {
		indexFilterFunction.filter(listFilter);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public IndexDtListFunctionBuilder<D> filterSubList(final int start, final int end) {
		indexFilterFunction.filterSubList(start, end);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public UnaryOperator<DtList<D>> build() {
		return indexFilterFunction;
	}
}
