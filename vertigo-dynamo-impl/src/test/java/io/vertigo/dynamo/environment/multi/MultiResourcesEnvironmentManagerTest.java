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
package io.vertigo.dynamo.environment.multi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.LogConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.NodeConfigBuilder;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.environment.multi.data.DtDefinitions;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;

/**
 * Test de l'implémentation standard.
 *
 * @author npiedeloup
 */
public final class MultiResourcesEnvironmentManagerTest {

	//
	//<module name="test-2"><!--  this moduleReference -->
	//	<resource type ="classes" path="io.vertigo.dynamock.domain.DtDefinitions"/>
	//</module>

	@Test
	public void testFirst() {
		final NodeConfig nodeConfig = prepareDefaultNodeConfigBuilder()
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/environment/multi/data/execution.kpr")
								.build())
						.build())
				.build();

		try (final AutoCloseableApp app = new AutoCloseableApp(nodeConfig)) {
			final Domain doString = app.getDefinitionSpace().resolve("DoString", Domain.class);
			Assertions.assertNotNull(doString);
		}
	}

	@Test
	public void testMergedResources() {
		final NodeConfig nodeConfig = prepareDefaultNodeConfigBuilder()
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/environment/multi/data/execution.kpr")
								.addDefinitionResource("classes", DtDefinitions.class.getCanonicalName()).build())
						.build())
				.build();

		try (final AutoCloseableApp app = new AutoCloseableApp(nodeConfig)) {
			final Domain doString = app.getDefinitionSpace().resolve("DoString", Domain.class);
			Assertions.assertNotNull(doString);
			final DtDefinition dtItem = app.getDefinitionSpace().resolve("DtItem", DtDefinition.class);
			Assertions.assertNotNull(dtItem);
		}
	}

	@Test
	public void testSplittedModules() {
		final NodeConfig nodeConfig = prepareDefaultNodeConfigBuilder()
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/environment/multi/data/execution.kpr")
								.addDefinitionResource("classes", DtDefinitions.class.getCanonicalName())
								.build())
						.build())
				.build();

		try (final AutoCloseableApp app = new AutoCloseableApp(nodeConfig)) {
			final Domain doString = app.getDefinitionSpace().resolve("DoString", Domain.class);
			Assertions.assertNotNull(doString);
			final DtDefinition dtItem = app.getDefinitionSpace().resolve("DtItem", DtDefinition.class);
			Assertions.assertNotNull(dtItem);
		}
	}

	private static NodeConfigBuilder prepareDefaultNodeConfigBuilder() {
		// @formatter:off
		return
			NodeConfig.builder()
			.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
				.withLocales("fr")
				.addPlugin(ClassPathResourceResolverPlugin.class)
			.endBoot();
		// @formatter:on
	}
}
