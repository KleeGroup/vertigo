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
package io.vertigo.dynamo.store.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamo.store.data.domain.famille.Famille;
import io.vertigo.dynamo.store.datastore.AbstractStoreManagerTest;

/**
 * Test de l'implémentation avec cache.
 *
 * @author pchretien
 */
public final class CachedStoreManagerTest extends AbstractStoreManagerTest {

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
								Param.of("dataBaseClass", H2DataBase.class.getName()),
								Param.of("jdbcDriver", "org.h2.Driver"),
								Param.of("jdbcUrl", "jdbc:h2:mem:database"))
						.build())
				.addModule(new DynamoFeatures()
						.withStore()
						.withSqlStore()
						.withDbFileStore(Param.of("storeDtName", "DtVxFileInfo"))
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/store/data/executionWfileinfo.kpr")
								.addDefinitionResource("classes", "io.vertigo.dynamo.store.data.DtDefinitions")
								.build())
						.addDefinitionProvider(TestCacheStoreManagerDefinitionProvider.class)
						.build())
				.build();
	}

	/**
	 * On charge une liste, ajoute un element et recharge la liste pour verifier l'ajout.
	 */
	@Override
	@Test
	public void testAddFamille() {
		//ce test est modifier car le cache n'est pas transactionnel : la liste n'est pas accessible sans commit
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final DtList<Famille> dtc = storeManager.getDataStore().find(dtDefinitionFamille, null, DtListState.of(null));
			Assertions.assertEquals(0, dtc.size());
			//-----
			final Famille famille = new Famille();
			famille.setLibelle("encore un");
			final Famille createdFamille = storeManager.getDataStore().create(famille);
			// on attend un objet avec un ID non null ?
			Assertions.assertNotNull(createdFamille.getFamId());
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final DtList<Famille> dtc = storeManager.getDataStore().find(dtDefinitionFamille, null, DtListState.of(null));
			Assertions.assertEquals(1, dtc.size());

		}
	}
}
