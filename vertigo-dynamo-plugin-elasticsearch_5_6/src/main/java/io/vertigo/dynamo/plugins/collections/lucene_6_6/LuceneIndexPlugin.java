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
package io.vertigo.dynamo.plugins.collections.lucene_6_6;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.commons.cache.CacheDefinition;
import io.vertigo.commons.cache.CacheManager;
import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.eventbus.EventBusSubscribed;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.impl.collections.IndexPlugin;
import io.vertigo.dynamo.store.StoreEvent;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Plugin de d'indexation de DtList utilisant Lucene en Ram.
 *
 * @author npiedeloup
 */
public final class LuceneIndexPlugin implements IndexPlugin, SimpleDefinitionProvider {

	private final CacheManager cacheManager;

	private static final String CACHE_LUCENE_INDEX = "CacheLuceneIndex";

	/**
	 * Constructor.
	 * @param localeManager Manager des messages localisés
	 * @param cacheManager Manager des caches
	 * @param eventBusManager Event manager
	 */
	@Inject
	public LuceneIndexPlugin(
			final LocaleManager localeManager,
			final CacheManager cacheManager,
			final EventBusManager eventBusManager) {
		Assertion.checkNotNull(localeManager);
		Assertion.checkNotNull(cacheManager);
		//-----
		this.cacheManager = cacheManager;
		localeManager.add(Resources.class.getName(), Resources.values());
	}

	/**
	 * Subscription to store events
	 * @param event the incomming event
	 */
	@EventBusSubscribed
	public void onStoreEvent(final StoreEvent event) {
		cacheManager.remove(CACHE_LUCENE_INDEX, getIndexCacheContext(event.getUID().getDefinition()));
	}

	/** {@inheritDoc} */
	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return Collections.singletonList(new CacheDefinition(CACHE_LUCENE_INDEX, false, 1000, 30 * 60, 60 * 60, true));
	}

	private <D extends DtObject> RamLuceneIndex<D> indexList(final DtList<D> fullDtc, final boolean storeValue) throws IOException {
		//TODO : gestion du cache a revoir... et le lien avec le CacheStore.
		//L'index devrait être interrogé par le Broker ? on pourrait alors mettre en cache dans le DataCache.
		final DtListURI dtcUri = fullDtc.getURI();
		final boolean useCache = dtcUri != null; //no cache if no URI
		RamLuceneIndex<D> index;
		if (useCache) {
			final String indexName = "INDEX_" + dtcUri.urn();
			final String cacheContext = getIndexCacheContext(fullDtc.getDefinition());
			//TODO non threadSafe.
			Map<String, RamLuceneIndex> luceneIndexMap = Map.class.cast(cacheManager.get(CACHE_LUCENE_INDEX, cacheContext));
			if (luceneIndexMap == null) {
				luceneIndexMap = new HashMap<>();
			}
			if (!luceneIndexMap.containsKey(indexName)) {
				index = createIndex(fullDtc, storeValue);
				luceneIndexMap.put(indexName, index);
				cacheManager.put(CACHE_LUCENE_INDEX, cacheContext, luceneIndexMap);
				return index;
			}
			return luceneIndexMap.get(indexName);
		}
		return createIndex(fullDtc, storeValue);
	}

	private static String getIndexCacheContext(final DtDefinition dtDefinition) {
		return "IndexCache:" + dtDefinition.getName();
	}

	private static <D extends DtObject> RamLuceneIndex<D> createIndex(final DtList<D> fullDtc, final boolean storeValue) throws IOException {
		Assertion.checkNotNull(fullDtc);
		//-----
		final RamLuceneIndex<D> luceneDb = new RamLuceneIndex<>(fullDtc.getDefinition());
		luceneDb.addAll(fullDtc, storeValue);
		return luceneDb;
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> DtList<D> getCollection(
			final String keywords,
			final Collection<DtField> searchedFields,
			final List<ListFilter> listFilters,
			final DtListState listState,
			final Optional<DtField> boostedField,
			final DtList<D> dtc) {
		Assertion.checkArgument(listState.getMaxRows().isPresent(), "Can't return all results, you must define maxRows");
		try {
			final RamLuceneIndex<D> index = indexList(dtc, false);
			return index.getCollection(keywords, searchedFields, listFilters, listState, boostedField);
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Erreur d'indexation");
		}
	}
}
