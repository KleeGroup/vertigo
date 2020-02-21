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
package io.vertigo.dynamo.task.data.domain;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.lang.Assertion;

public final class SuperHeroDataBase {
	private final VTransactionManager transactionManager;
	private final TaskManager taskManager;

	public SuperHeroDataBase(final VTransactionManager transactionManager, final TaskManager taskManager) {
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(taskManager);
		//---
		this.transactionManager = transactionManager;
		this.taskManager = taskManager;
	}

	public void createDataBase() {
		//A chaque test on recrée la table SUPER_HERO
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			execStatement("create table SUPER_HERO(id BIGINT , name varchar(255));");
			execStatement("create sequence SEQ_SUPER_HERO start with 10001 increment by 1");
		}
	}

	private void execStatement(final String request) {
		final TaskDefinition taskDefinition = TaskDefinition.builder("TkInit")
				.withEngine(TaskEngineProc.class)
				.withRequest(request)
				.build();
		final Task task = Task.builder(taskDefinition).build();
		taskManager.execute(task);
	}

	public static DtList<SuperHero> getSuperHeroes() {
		return DtList.of(
				createSuperHero(1, "superman"),
				createSuperHero(2, "batman"),
				createSuperHero(3, "catwoman"),
				createSuperHero(4, "wonderwoman"),
				createSuperHero(5, "aquaman"),
				createSuperHero(6, "green lantern"),
				createSuperHero(7, "captain america"),
				createSuperHero(8, "spiderman"));
	}

	private static SuperHero createSuperHero(final long id, final String name) {
		final SuperHero superHero = new SuperHero();
		superHero.setId(id);
		superHero.setName(name);
		return superHero;
	}

	public void populateSuperHero(final StoreManager storeManager, final int size) {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//-----
			for (int i = 0; i < size; i++) {
				final SuperHero superHero = new SuperHero();
				superHero.setName("SuperHero ( " + i + ")");
				storeManager.getDataStore().create(superHero);
			}
			transaction.commit();
		}
	}
}
