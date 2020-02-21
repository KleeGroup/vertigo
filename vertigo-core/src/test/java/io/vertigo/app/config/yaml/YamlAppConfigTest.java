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
package io.vertigo.app.config.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.core.spaces.component.data.BioManager;

public final class YamlAppConfigTest {

	@Test
	public void testBoot() {

		final NodeConfig nodeConfig = new YamlAppConfigBuilder(new Properties())
				.withFiles(getClass(), "bio-boot.yaml")
				.build();

		testBioManager(nodeConfig);
	}

	@Test
	public void testNoBoot() {

		final NodeConfig nodeConfig = new YamlAppConfigBuilder(new Properties())
				.withFiles(getClass(), "bio.yaml")
				.build();

		testBioManager(nodeConfig);
	}

	@Test
	public void testNodeConfig() {

		final NodeConfig nodeConfig = new YamlAppConfigBuilder(new Properties())
				.withFiles(getClass(), "bio-node.yaml")
				.build();

		testBioManager(nodeConfig);

		assertEquals("bio", nodeConfig.getAppName());
		assertEquals("myFirstNodeId", nodeConfig.getNodeId());
		assertEquals("http://localhost/", nodeConfig.getEndPoint().get());
	}

	@Test
	public void testActiveFlagsMainConfig() {
		final Properties params = new Properties();
		params.setProperty("boot.activeFlags", "main");
		final NodeConfig nodeConfig = new YamlAppConfigBuilder(params)
				.withFiles(getClass(), "bio-flags.yaml")
				.build();

		testBioManager(nodeConfig);
	}

	@Test
	public void testActiveFlagsSecondaryConfig() {
		final Properties params = new Properties();
		params.setProperty("boot.activeFlags", "secondary");
		final NodeConfig nodeConfig = new YamlAppConfigBuilder(params)
				.withFiles(getClass(), "bio-flags.yaml")
				.build();

		try (AutoCloseableApp app = new AutoCloseableApp(nodeConfig)) {
			assertEquals(app, app);
			assertTrue(app.getComponentSpace().contains("bioManager"));
			final BioManager bioManager = app.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			assertEquals(336, res);
			assertTrue(bioManager.isActive());
		}
	}

	@Test
	public void testNegateFlagsConfig() {
		final Properties params = new Properties();
		params.setProperty("boot.activeFlags", "main;customStart");
		final NodeConfig nodeConfig = new YamlAppConfigBuilder(params)
				.withFiles(getClass(), "bio-flags.yaml")
				.build();

		try (AutoCloseableApp app = new AutoCloseableApp(nodeConfig)) {
			assertEquals(app, app);
			assertTrue(app.getComponentSpace().contains("bioManager"));
			final BioManager bioManager = app.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			assertEquals(666, res);
			assertTrue(bioManager.isActive());
		}
	}

	private void testBioManager(final NodeConfig nodeConfig) {
		try (AutoCloseableApp app = new AutoCloseableApp(nodeConfig)) {
			assertEquals(app, app);
			assertTrue(app.getComponentSpace().contains("bioManager"));
			final BioManager bioManager = app.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			assertEquals(366, res);
			assertTrue(bioManager.isActive());
		}
	}
}
