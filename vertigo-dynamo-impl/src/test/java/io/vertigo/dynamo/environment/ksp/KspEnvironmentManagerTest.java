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
package io.vertigo.dynamo.environment.ksp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.NodeConfigBuilder;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.lang.Assertion;

/**
 * Test ksp model.
 *
 * @author mlaroche
 */
public final class KspEnvironmentManagerTest {

	@Test
	public void testDomain() {
		final NodeConfigBuilder nodeConfigBuilder = createNodeConfigBuilder("io/vertigo/dynamo/environment/ksp/data/execution.kpr");
		try (final AutoCloseableApp app = new AutoCloseableApp(nodeConfigBuilder.build())) {
			//nothing (if it's boot it's ok)

		}
	}

	@Test
	public void testWrongNavigability() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			final NodeConfigBuilder nodeConfigBuilder = createNodeConfigBuilder("io/vertigo/dynamo/environment/ksp/data/execution-forbidden.kpr");
			try (final AutoCloseableApp app = new AutoCloseableApp(nodeConfigBuilder.build())) {
				//nothing (exception is ok)

			}
		});
	}

	@Test
	public void testNonPossibleAssociation() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			final NodeConfigBuilder nodeConfigBuilder = createNodeConfigBuilder("io/vertigo/dynamo/environment/ksp/data/execution-forbidden2.kpr");
			try (final AutoCloseableApp app = new AutoCloseableApp(nodeConfigBuilder.build())) {
				//nothing (exception is ok)

			}
		});
	}

	private static NodeConfigBuilder createNodeConfigBuilder(final String kprPath) {
		Assertion.checkArgNotEmpty(kprPath);
		//---
		return NodeConfig.builder()
				.beginBoot()
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(ModuleConfig.builder("myModule")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", kprPath)
								.build())
						.build());
	}

}
