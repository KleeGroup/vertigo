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
package io.vertigo.dynamo.search_5_6.withstore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.statement.SqlStatement;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.data.domain.Item;
import io.vertigo.dynamo.search.data.domain.ItemDataBase;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.dynamo.store.StoreManager;

/**
 * Test de l'implémentation standard couplé au store.
 *
 * @author npiedeloup
 */
public class SearchManagerStoreTest extends AbstractTestCaseJU5 {
	@Inject
	private SqlDataBaseManager dataBaseManager;
	@Inject
	private StoreManager storeManager;
	@Inject
	private VTransactionManager transactionManager;
	@Inject
	private SearchManager searchManager;
	//Index
	private static final String IDX_ITEM = "IdxItem";

	private SearchIndexDefinition itemIndexDefinition;

	private long initialDbItemSize = 0;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.withLocales("fr_FR")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(new CommonsFeatures()
						.withCache()
						.withScript()
						.withMemoryCache()
						.withJaninoScript()
						.build())
				.addModule(new DatabaseFeatures()
						.withSqlDataBase()
						.withC3p0(
								Param.of("dataBaseClass", "io.vertigo.database.impl.sql.vendor.h2.H2DataBase"),
								Param.of("jdbcDriver", "org.h2.Driver"),
								Param.of("jdbcUrl", "jdbc:h2:mem:database"))
						.build())
				.addModule(new DynamoFeatures()
						.withStore()
						.withSearch()
						.withSqlStore()
						.addPlugin(io.vertigo.dynamo.plugins.search.elasticsearch_5_6.embedded.ESEmbeddedSearchServicesPlugin.class,
								Param.of("home", "io/vertigo/dynamo/search_5_6/indexconfig"),
								Param.of("config.file", "io/vertigo/dynamo/search_5_6/indexconfig/elasticsearch.yml"),
								Param.of("envIndex", "TuTest"),
								Param.of("rowsPerQuery", "50"))
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/search/data/execution.kpr")
								.addDefinitionResource("classes", "io.vertigo.dynamo.search.data.DtDefinitions")
								.build())
						.addComponent(ItemSearchLoader.class)
						.build())
				.build();
	}

	@Override
	protected void doSetUp() throws SQLException {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		itemIndexDefinition = definitionSpace.resolve(IDX_ITEM, SearchIndexDefinition.class);

		//A chaque test on recrée la table famille
		try (final SqlConnectionCloseable connectionCloseable = new SqlConnectionCloseable(dataBaseManager)) {
			execCallableStatement(connectionCloseable.getConnection(), "create table item(ID BIGINT, MANUFACTURER varchar(50), MODEL varchar(255), DESCRIPTION varchar(512), YEAR INT, KILO INT, PRICE INT, CONSOMMATION NUMERIC(8,2), MOTOR_TYPE varchar(50), OPTIONAL_NUMBER BIGINT, OPTIONAL_STRING varchar(50), LAST_MODIFIED timestamp );");
			execCallableStatement(connectionCloseable.getConnection(), "create sequence SEQ_ITEM start with 10001 increment by 1");
		}

		//On supprime tout
		remove("*:*");

		final ItemDataBase itemDataBase = new ItemDataBase();
		initialDbItemSize = itemDataBase.size();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			for (final Item item : itemDataBase.getAllItems()) {
				item.setId(null);
				storeManager.getDataStore().create(item);
			}
			transaction.commit();
		}
		waitIndexation();
	}

	/** {@inheritDoc} */
	@Override
	protected void doTearDown() throws SQLException {
		//A chaque fin de test on arréte la base.
		try (final SqlConnectionCloseable connectionCloseable = new SqlConnectionCloseable(dataBaseManager)) {
			execCallableStatement(connectionCloseable.getConnection(), "shutdown;");
		}
	}

	private void execCallableStatement(final SqlConnection connection, final String sql) throws SQLException {
		dataBaseManager.executeUpdate(
				SqlStatement.builder(sql).build(),
				connection);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexAllQuery() {
		final long size = query("*:*");
		Assertions.assertEquals(initialDbItemSize, size);
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexNewData() {
		testIndexAllQuery();

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Item item = createNewItem();
			storeManager.getDataStore().create(item);
			transaction.commit();
		}
		waitIndexation();
		Assertions.assertEquals(initialDbItemSize + 1, query("*:*"));
		Assertions.assertEquals(1, query("description:légende"));
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexDeleteData() {
		testIndexAllQuery();
		Assertions.assertEquals(1, query("id:10001"));

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().delete(createURI(10001L));
			transaction.commit();
		}
		waitIndexation();
		Assertions.assertEquals(0, query("id:10001"));
		Assertions.assertEquals(initialDbItemSize - 1, query("*:*"));
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexUpdateData() {
		testIndexAllQuery();
		final Item item = createNewItem();

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().create(item);
			transaction.commit();
		}

		waitIndexation();
		Assertions.assertEquals(initialDbItemSize + 1, query("*:*"));
		Assertions.assertEquals(1, query("description:légende"));

		item.setDescription("Vendue");
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().update(item);
			transaction.commit();
		}

		waitIndexation();
		Assertions.assertEquals(initialDbItemSize + 1, query("*:*"));
		Assertions.assertEquals(0, query("description:légende"));
		Assertions.assertEquals(1, query("description:vendue"));
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexDoubleUpdateData() {
		testIndexAllQuery();
		final Item item = createNewItem();

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().create(item);
			transaction.commit();
		}

		waitIndexation();
		Assertions.assertEquals(initialDbItemSize + 1, query("*:*"));
		Assertions.assertEquals(1, query("description:légende"));

		item.setDescription("Vendue");
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().update(item);
			storeManager.getDataStore().update(item);
			storeManager.getDataStore().update(item);
			storeManager.getDataStore().update(item);
			transaction.commit();
		}

		waitIndexation();
		Assertions.assertEquals(initialDbItemSize + 1, query("*:*"));
		Assertions.assertEquals(0, query("description:légende"));
		Assertions.assertEquals(1, query("description:vendue"));
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testRemoveAll() {
		//On supprime tout
		remove("*:*");
		final long resize = query("*:*");
		Assertions.assertEquals(0L, resize);
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testReIndexAll() {
		testIndexAllQuery();
		remove("*:*");
		long resize = query("*:*");
		Assertions.assertEquals(0L, resize);

		doReindexAll();
		waitIndexation();

		resize = query("*:*");
		Assertions.assertEquals(initialDbItemSize, resize);
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 * @throws SQLException if some error in test
	 */
	@Test
	public void testReIndexAllAfterDirectDelete() throws SQLException {
		testIndexAllQuery();
		long resize = query("*:*");
		Assertions.assertEquals(initialDbItemSize, resize);

		try (final SqlConnectionCloseable connectionCloseable = new SqlConnectionCloseable(dataBaseManager)) {
			execCallableStatement(connectionCloseable.getConnection(), "delete from item where id = 10001");
			connectionCloseable.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals(initialDbItemSize - 1, storeManager.getDataStore().count(itemIndexDefinition.getKeyConceptDtDefinition()));
		}
		doReindexAll();
		waitIndexation();

		resize = query("*:*");
		Assertions.assertEquals(initialDbItemSize - 1, resize);
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 * @throws SQLException if some error in test
	 */
	@Test
	public void testReIndexAllAfterDirectDeleteAll() throws SQLException {
		testIndexAllQuery();
		long resize = query("*:*");
		Assertions.assertEquals(initialDbItemSize, resize);

		try (final SqlConnectionCloseable connectionCloseable = new SqlConnectionCloseable(dataBaseManager)) {
			execCallableStatement(connectionCloseable.getConnection(), "delete from item ");
			connectionCloseable.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals(0, storeManager.getDataStore().count(itemIndexDefinition.getKeyConceptDtDefinition()));
		}
		doReindexAll();
		waitIndexation();

		resize = query("*:*");
		Assertions.assertEquals(0, resize);
	}

	private static Item createNewItem() {
		final Item item = new Item();
		item.setId(null);
		item.setPrice(12000);
		item.setManufacturer("Acme");
		item.setModel("Martin");
		item.setYear(1978);
		item.setKilo(1500);
		final BigDecimal conso = new BigDecimal(7.6);
		conso.setScale(2, RoundingMode.HALF_UP);
		item.setConsommation(conso);
		item.setMotorType("essence");
		item.setDescription("Voiture de légende assurant une reindexation dès son insertion");
		return item;
	}

	private long query(final String query) {
		//recherche
		final SearchQuery searchQuery = SearchQuery.builder(ListFilter.of(query))
				.build();

		return doQuery(searchQuery, null).getCount();
	}

	private FacetedQueryResult<Item, SearchQuery> doQuery(final SearchQuery searchQuery, final DtListState listState) {
		return searchManager.loadList(itemIndexDefinition, searchQuery, listState);
	}

	private static UID createURI(final long itemId) {
		return UID.of(Item.class, itemId);
	}

	protected void remove(final String query) {
		doRemove(query);
		waitIndexation();
	}

	private void doRemove(final String query) {
		final ListFilter removeQuery = ListFilter.of(query);
		searchManager.removeAll(itemIndexDefinition, removeQuery);
	}

	private void doReindexAll() {
		searchManager.reindexAll(itemIndexDefinition);
		try {
			Thread.sleep(5000); //wait reindex job
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt(); //si interrupt on relance
		}
	}

	private static void waitIndexation() {
		try {
			Thread.sleep(1000 + 1500); //wait index was done
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt(); //si interrupt on relance
		}
	}

	private class SqlConnectionCloseable implements AutoCloseable {
		private final SqlConnection connection;

		SqlConnectionCloseable(final SqlDataBaseManager dataBaseManager) {
			connection = dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME).obtainConnection();
		}

		SqlConnection getConnection() {
			return connection;
		}

		public void commit() throws SQLException {
			connection.commit();
		}

		@Override
		public void close() throws SQLException {
			connection.release();
		}
	}

}
