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
package io.vertigo.dynamo.search.withstore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.data.domain.Item;
import io.vertigo.dynamo.search.metamodel.SearchChunk;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamox.search.AbstractSqlSearchLoader;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.lang.Assertion;

/**
 * SearchLoader of Item keyconcept, load uses StoreManager.
 * @author npiedeloup
 */
public final class ItemSearchLoader extends AbstractSqlSearchLoader<Long, Item, Item> {
	private final SearchManager searchManager;
	private final DefinitionSpace definitionSpace;

	/**
	 * Constructor.
	 * @param taskManager Task manager
	 * @param searchManager Search manager
	 */
	@Inject
	public ItemSearchLoader(final TaskManager taskManager, final SearchManager searchManager, final VTransactionManager transactionManager) {
		super(taskManager, transactionManager);
		Assertion.checkNotNull(searchManager);
		//---
		this.searchManager = searchManager;
		definitionSpace = Home.getApp().getDefinitionSpace();
	}

	/** {@inheritDoc} */
	@Override
	public List<SearchIndex<Item, Item>> loadData(final SearchChunk<Item> searchChunk) {
		final SearchIndexDefinition indexDefinition = searchManager.findFirstIndexDefinitionByKeyConcept(Item.class);
		try (final VTransactionWritable tx = getTransactionManager().createCurrentTransaction()) {
			final List<SearchIndex<Item, Item>> result = new ArrayList<>();
			for (final Item item : loadItems(searchChunk)) {
				final UID<Item> uid = item.getUID();
				result.add(SearchIndex.createIndex(indexDefinition, uid, item));
			}
			return result;
		}
	}

	private DtList<Item> loadItems(final SearchChunk<Item> searchChunk) {
		final TaskDefinition taskDefinition = getTaskDefinition(searchChunk);

		final Task task = Task.builder(taskDefinition)
				.build();

		return getTaskManager()
				.execute(task)
				.getResult();
	}

	private TaskDefinition getTaskDefinition(final SearchChunk<Item> searchChunk) {
		final Domain doItems = definitionSpace.resolve("DoDtItemDtc", Domain.class);
		final String sql = searchChunk.getAllUIDs()
				.stream()
				.map(uri -> uri.getId().toString())
				.collect(Collectors.joining(", ", "select * from ITEM where ID in (", ")"));

		return TaskDefinition.builder("TkLoadAllItems")
				.withEngine(TaskEngineSelect.class)
				.withRequest(sql)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.withOutRequired("dtc", doItems)
				.build();
	}
}
