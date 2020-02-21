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
package io.vertigo.dynamox.metrics;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.metric.Metric;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.data.domain.SuperHeroDataBase;
import io.vertigo.dynamo.task.x.SuperHeroDao;
import io.vertigo.dynamox.metric.domain.DomainMetricsProvider;
import io.vertigo.dynamox.metric.task.TasksMetricsProvider;

/**
 * @author mlaroche
 */
public final class MetricAnalyticsTest extends AbstractTestCaseJU5 {

	@Inject
	private AnalyticsManager analyticsManager;
	@Inject
	private TaskManager taskManager;
	@Inject
	private StoreManager storeManager;
	@Inject
	private VTransactionManager transactionManager;

	private SuperHeroDataBase superHeroDataBase;

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
						.withTaskProxyMethod()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/task/data/executionWTask.kpr")
								.addDefinitionResource("classes", "io.vertigo.dynamo.task.data.DtDefinitions")
								.build())
						.addProxy(SuperHeroDao.class)
						.build())
				.addModule(ModuleConfig.builder("analytics-metric")
						.addComponent(DomainMetricsProvider.class)
						.addComponent(TasksMetricsProvider.class)
						.build())
				.build();
	}

	@Override
	protected void doSetUp() throws Exception {
		superHeroDataBase = new SuperHeroDataBase(transactionManager, taskManager);
		superHeroDataBase.createDataBase();
		superHeroDataBase.populateSuperHero(storeManager, 33);
	}

	@Test
	public void testAnalyze() {
		final List<Metric> metrics = analyticsManager.getMetrics();
		//---
		Assertions.assertEquals(35, metrics.size());
	}
}
