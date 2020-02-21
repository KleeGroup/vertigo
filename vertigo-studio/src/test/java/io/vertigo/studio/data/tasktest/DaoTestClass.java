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
package io.vertigo.studio.data.tasktest;

import javax.inject.Inject;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.App;
import io.vertigo.app.Home;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.LogConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.core.plugins.resource.local.LocalResourceResolverPlugin;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.plugins.sql.connection.c3p0.C3p0ConnectionProviderPlugin;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamo.plugins.store.datastore.sql.SqlDataStorePlugin;
import io.vertigo.studio.mda.DataBaseScriptUtil;
import io.vertigo.studio.plugins.mda.task.test.TaskTestDaoChecker;
import io.vertigo.studio.plugins.mda.task.test.TaskTestDummyGenerator;
import io.vertigo.studio.plugins.mda.task.test.TaskTestDummyGeneratorBasic;
import io.vertigo.studio.tasktest.DaoPAO;

public class DaoTestClass extends AbstractTestCaseJU5 {

	@Inject
	private VTransactionManager transactionManager;

	private VTransactionWritable currentTransaction;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.beginBoot()
				.withLocales("fr")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.addPlugin(LocalResourceResolverPlugin.class)
				.withLogConfig(new LogConfig("/log4j.xml"))
				.endBoot()
				.addModule(new CommonsFeatures()
						.withCache()
						.withMemoryCache()
						.withScript()
						.withJaninoScript()
						.build())
				.addModule(new DatabaseFeatures()
						.withSqlDataBase()
						.addPlugin(C3p0ConnectionProviderPlugin.class,
								Param.of("dataBaseClass", "io.vertigo.database.impl.sql.vendor.h2.H2DataBase"),
								Param.of("jdbcDriver", "org.h2.Driver"),
								Param.of("jdbcUrl", "jdbc:h2:mem:database"))
						.build())
				.addModule(new DynamoFeatures()
						.withStore()
						.addPlugin(SqlDataStorePlugin.class,
								Param.of("sequencePrefix", "SEQ_"))
						.build())
				.addModule(ModuleConfig.builder("dao")
						// to use this class for actual test target/javagen must contains those two dao classes and target/javagen must be included as a source folder
						// .addComponent(CarDAO.class)
						.addComponent(DaoPAO.class)
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("classes", "io.vertigo.studio.data.DtDefinitions")
								.addDefinitionResource("kpr", "io/vertigo/studio/data/generationWTask.kpr")
								.build())
						.build())
				.build();
	}

	@Override
	protected void doSetUp() throws Exception {
		execSqlScript("io/vertigo/studio/data/sql/crebas.sql", Home.getApp());
		currentTransaction = transactionManager.createCurrentTransaction();
	}

	@Override
	protected void doTearDown() throws Exception {
		currentTransaction.rollback();
	}

	private void execSqlScript(final String sqlScript, final App app) {
		final ResourceManager resourceManager = app.getComponentSpace().resolve(ResourceManager.class);
		final SqlDataBaseManager sqlDataBaseManager = app.getComponentSpace().resolve(SqlDataBaseManager.class);

		final SqlConnection connection = sqlDataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME).obtainConnection();
		DataBaseScriptUtil.execSqlScript(connection, sqlScript, resourceManager, sqlDataBaseManager);
	}

	private final TaskTestDummyGenerator dum = new TaskTestDummyGeneratorBasic();
	private final TaskTestDaoChecker check = new TaskTestDaoChecker();

	public TaskTestDummyGenerator dum() {
		return dum;
	}

	public TaskTestDaoChecker check() {
		return check;
	}
}
