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
package io.vertigo.dynamox.search;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.metamodel.SearchChunk;
import io.vertigo.dynamo.search.metamodel.SearchLoader;

/**
 * Abstract SearchLoader with default chunk implementation.
 * @author npiedeloup
 * @param <P> Primary key type
 * @param <K> KeyConcept type
 * @param <I> Index type
 */
public abstract class AbstractSearchLoader<P extends Serializable, K extends KeyConcept, I extends DtObject> implements
		SearchLoader<K, I> {

	/** {@inheritDoc} */
	@Override
	public final Iterable<SearchChunk<K>> chunk(final Class<K> keyConceptClass) {
		return () -> createIterator(keyConceptClass);
	}

	private List<UID<K>> doLoadNextURI(final P lastId, final DtDefinition dtDefinition) {
		return loadNextURI(lastId, dtDefinition);
	}

	/**
	 * Load uris of next chunk.
	 * @param lastId Last chunk id
	 * @param dtDefinition KeyConcept definition
	 * @return Uris of next chunk.
	 */
	protected abstract List<UID<K>> loadNextURI(final P lastId, final DtDefinition dtDefinition);

	private P getLowestIdValue(final DtDefinition dtDefinition) {
		final DtField idField = dtDefinition.getIdField().get();
		final DataType idDataType = idField.getDomain().getDataType();
		P pkValue;
		switch (idDataType) {
			case Integer:
				pkValue = (P) Integer.valueOf(-1);
				break;
			case Long:
				pkValue = (P) Long.valueOf(-1);
				break;
			case String:
				pkValue = (P) "";
				break;
			case BigDecimal:
			case DataStream:
			case Boolean:
			case Double:
			case LocalDate:
			case Instant:
			default:
				throw new IllegalArgumentException("Type's PK " + idDataType.name() + " of "
						+ dtDefinition.getClassSimpleName() + " is not supported, prefer int, long or String ID.");
		}
		return pkValue;
	}

	private Iterator<SearchChunk<K>> createIterator(final Class<K> keyConceptClass) {
		return new Iterator<SearchChunk<K>>() {
			private final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(keyConceptClass);
			private SearchChunk<K> current;
			private SearchChunk<K> next = firstChunk();

			/** {@inheritDoc} */
			@Override
			public boolean hasNext() {
				if (next == null) {
					next = nextChunk(current);
				}
				return !next.getAllUIDs().isEmpty();
			}

			/** {@inheritDoc} */
			@Override
			public SearchChunk<K> next() {
				if (!hasNext()) {
					throw new NoSuchElementException("no next chunk found");
				}
				current = next;
				next = null;
				return current;
			}

			private SearchChunk<K> nextChunk(final SearchChunk<K> previousChunk) {
				final List<UID<K>> previousUris = previousChunk.getAllUIDs();
				final P lastId = (P) previousUris.get(previousUris.size() - 1).getId();
				// call loader service
				final List<UID<K>> uris = doLoadNextURI(lastId, dtDefinition);
				return new SearchChunk<>(uris);
			}

			private SearchChunk<K> firstChunk() {
				final P lastId = getLowestIdValue(dtDefinition);
				// call loader service
				final List<UID<K>> uris = doLoadNextURI(lastId, dtDefinition);
				return new SearchChunk<>(uris);
			}

		};
	}
}
