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
package io.vertigo.dynamo.criteria.sql;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;

import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.criteria.AbstractCriteriaTest;
import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.data.movies.Movie2;
import io.vertigo.dynamo.criteria.data.movies.Movie2DataBase;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.datastore.SqlUtil;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.util.ListBuilder;

/**
 *
 */
public final class SqlCriteriaTest extends AbstractCriteriaTest {
	@Inject
	protected StoreManager storeManager;
	@Inject
	protected FileManager fileManager;
	@Inject
	protected VTransactionManager transactionManager;
	@Inject
	protected TaskManager taskManager;

	private DtDefinition dtDefinitionMovie;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.withLocales("fr_FR")
				.endBoot()
				.addModule(new CommonsFeatures()
						.withScript()
						.withJaninoScript()
						.withCache()
						.withMemoryCache()
						.build())
				.addModule(new DatabaseFeatures()
						.withSqlDataBase()
						.withC3p0(
								Param.of("dataBaseClass", H2DataBase.class.getName()),
								Param.of("jdbcDriver", "org.h2.Driver"),
								Param.of("jdbcUrl", "jdbc:h2:mem:database"))
						.build())
				.addModule(new DynamoFeatures()
						.withStore()
						.withSqlStore()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/criteria/data/execution.kpr")
								.addDefinitionResource("classes", "io.vertigo.dynamo.criteria.data.DtDefinitions")
								.build())
						.build())
				.build();
	}

	@Override
	protected void doSetUp() throws Exception {
		dtDefinitionMovie = DtObjectUtil.findDtDefinition(Movie2.class);
		initMainStore();
	}

	private void initMainStore() {
		//A chaque test on recrée la table famille
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				getCreateMovies(),
				"TkInitMain",
				Optional.empty());

		final Movie2DataBase movie2DataBase = new Movie2DataBase();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			movie2DataBase.getAllMovies().forEach(movie2 -> storeManager.getDataStore().create(movie2));
			transaction.commit();
		}
	}

	protected List<String> getCreateMovies() {
		return new ListBuilder<String>()
				.add(" create table movie_2(id BIGINT , TITLE varchar(50), YEAR INT);")
				.add(" create sequence SEQ_MOVIE_2 start with 1 increment by 1;")
				.build();
	}

	@Override
	public void assertCriteria(final long expected, final Criteria<Movie2> criteria) {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final long count = storeManager.getDataStore().find(dtDefinitionMovie, criteria, DtListState.of(null)).size();
			Assertions.assertEquals(expected, count);
		}
	}

}
