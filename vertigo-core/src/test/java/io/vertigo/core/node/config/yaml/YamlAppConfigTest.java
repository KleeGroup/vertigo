/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.config.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.data.BioManager;
import io.vertigo.core.node.config.NodeConfig;

public final class YamlAppConfigTest {

	@Test
	public void testBoot() {

		final NodeConfig nodeConfig = new YamlNodeConfigBuilder(new Properties())
				.withFiles(getClass(), "bio-boot.yaml")
				.build();

		testBioManager(nodeConfig);
	}

	@Test
	public void testBootParams() {
		final Properties params = new Properties();
		params.setProperty("boot.testProperties", "io/vertigo/core/node/config/yaml/test-params.properties");
		final NodeConfig nodeConfig = new YamlNodeConfigBuilder(params)
				.withFiles(getClass(), "bio-boot-params.yaml")
				.build();

		testBioManager(nodeConfig);
	}

	@Test
	public void testNoBoot() {

		final NodeConfig nodeConfig = new YamlNodeConfigBuilder(new Properties())
				.withFiles(getClass(), "bio.yaml")
				.build();

		testBioManager(nodeConfig);
	}

	@Test
	public void testNodeConfig() {

		final NodeConfig nodeConfig = new YamlNodeConfigBuilder(new Properties())
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
		final NodeConfig nodeConfig = new YamlNodeConfigBuilder(params)
				.withFiles(getClass(), "bio-flags.yaml")
				.build();

		testBioManager(nodeConfig);
	}

	@Test
	public void testActiveFlagsSecondaryConfig() {
		final Properties params = new Properties();
		params.setProperty("boot.activeFlags", "secondary");
		final NodeConfig nodeConfig = new YamlNodeConfigBuilder(params)
				.withFiles(getClass(), "bio-flags.yaml")
				.build();

		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			assertEquals(node, node);
			assertTrue(node.getComponentSpace().contains("bioManager"));
			final BioManager bioManager = node.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			assertEquals(336, res);
			assertTrue(bioManager.isActive());
		}
	}

	@Test
	public void testNegateFlagsConfig() {
		final Properties params = new Properties();
		params.setProperty("boot.activeFlags", "main;customStart");
		final NodeConfig nodeConfig = new YamlNodeConfigBuilder(params)
				.withFiles(getClass(), "bio-flags.yaml")
				.build();

		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			assertEquals(node, node);
			assertTrue(node.getComponentSpace().contains("bioManager"));
			final BioManager bioManager = node.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			assertEquals(666, res);
			assertTrue(bioManager.isActive());
		}
	}

	@Test
	public void testFull() {
		final NodeConfig nodeConfig = new YamlNodeConfigBuilder(new Properties())
				.withFiles(getClass(), "bio-full.yaml")
				.build();
		testBioManager(nodeConfig);
	}

	private void testBioManager(final NodeConfig nodeConfig) {
		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			assertEquals(node, node);
			assertTrue(node.getComponentSpace().contains("bioManager"));
			final BioManager bioManager = node.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			assertEquals(366, res);
			assertTrue(bioManager.isActive());
		}
	}
}
