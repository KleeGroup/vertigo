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
package io.vertigo.core.node.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.data.BioManager;
import io.vertigo.core.node.component.data.BioManagerImpl;
import io.vertigo.core.node.component.data.MathManager;
import io.vertigo.core.node.component.data.MathManagerImpl;
import io.vertigo.core.node.component.data.SimpleDummyPlugin;
import io.vertigo.core.node.component.data.SimpleMathPlugin;
import io.vertigo.core.node.component.data.SomeConnector;
import io.vertigo.core.node.component.data.SomeManager;
import io.vertigo.core.node.component.data.SomeMonoConnectorPlugin;
import io.vertigo.core.node.component.data.SomeMultiConnectorPlugin;
import io.vertigo.core.node.component.data.SomeOptionalPlugin;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.LogConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;

public final class ComponentSpaceTest {

	@Test
	public void testHome() {
		final NodeConfig nodeConfig = NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLogConfig(new LogConfig("/log4j.xml"))
						.build())
				.addModule(ModuleConfig.builder("Bio")
						.addComponent(BioManager.class, BioManagerImpl.class)
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(SimpleMathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();

		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			final BioManager bioManager = node.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			assertEquals(366, res);
			assertTrue(bioManager.isActive());
		}
	}

	@Test
	public void testHome2() {
		final NodeConfig nodeConfig = NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLogConfig(new LogConfig("/log4j.xml"))
						.build())
				.addModule(ModuleConfig.builder("Bio")
						.addComponent(BioManager.class, BioManagerImpl.class)
						//This plugin DummyPlugin is not used By BioManager !!
						.addPlugin(SimpleDummyPlugin.class)
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(SimpleMathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();

		Assertions.assertThrows(RuntimeException.class,
				() -> {
					try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
						//
					}
				});
	}

	@Test
	public void testHome3() {
		final NodeConfig nodeConfig = NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLogConfig(new LogConfig("/log4j.xml"))
						.build())
				.addModule(ModuleConfig.builder("Bio-core")
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(SimpleMathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.addModule(ModuleConfig.builder("Bio-spe") //This module depends of Bio-core module
						.addComponent(BioManager.class, BioManagerImpl.class)
						.build())
				.build();

		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			//
		}
	}

	@Test
	public void testOneConnector() {
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("Bio")
						.addComponent(SomeManager.class)
						.addConnector(SomeConnector.class, Param.of("name", "main"))
						.addPlugin(SomeMonoConnectorPlugin.class)
						.build())
				.build();

		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			final SomeManager manager = Node.getNode().getComponentSpace().resolve(SomeManager.class);
			Assertions.assertEquals("main", manager.getSomeNames());
		}
	}

	@Test
	public void testTwoConnectors() {
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("Bio")
						.addComponent(SomeManager.class)
						.addConnector(SomeConnector.class, Param.of("name", "first"))
						.addConnector(SomeConnector.class, Param.of("name", "second"))
						.addPlugin(SomeMultiConnectorPlugin.class)
						.build())
				.build();

		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			final SomeManager manager = Node.getNode().getComponentSpace().resolve(SomeManager.class);
			Assertions.assertEquals("first,second", manager.getSomeNames());
		}
	}

	@Test
	public void testOutsideModuleConnectors() {
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("Connector")
						.addConnector(SomeConnector.class, Param.of("name", "first"))
						.addConnector(SomeConnector.class, Param.of("name", "second"))
						.build())
				.addModule(ModuleConfig.builder("Bio")
						.addComponent(SomeManager.class)
						.addPlugin(SomeMultiConnectorPlugin.class)
						.build())
				.build();

		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			final SomeManager manager = Node.getNode().getComponentSpace().resolve(SomeManager.class);
			Assertions.assertEquals("first,second", manager.getSomeNames());
		}
	}

	@Test
	public void testOutsideModuleOptionalConnector() {
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("Connector")
						.addConnector(SomeConnector.class, Param.of("name", "first"))
						.build())
				.addModule(ModuleConfig.builder("Bio")
						.addComponent(SomeManager.class)
						.addPlugin(SomeOptionalPlugin.class)
						.build())
				.build();

		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			final SomeManager manager = Node.getNode().getComponentSpace().resolve(SomeManager.class);
			Assertions.assertEquals("first", manager.getSomeNames());
		}
	}

	@Test
	public void testOutsideModuleOptionalConnector2() {
		final NodeConfig nodeConfig = NodeConfig.builder()
				.addModule(ModuleConfig.builder("Bio")
						.addComponent(SomeManager.class)
						.addPlugin(SomeOptionalPlugin.class)
						.build())
				.build();

		try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
			final SomeManager manager = Node.getNode().getComponentSpace().resolve(SomeManager.class);
			Assertions.assertEquals("none", manager.getSomeNames());
		}
	}

}
