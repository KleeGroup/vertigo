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
package io.vertigo.core.node.component.amplifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.amplifier.data.Aggregate;
import io.vertigo.core.node.component.amplifier.data.AggregatorProxyMethod;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;

public class AmplifierTest {

	@Inject
	private Aggregate aggregatea;

	private AutoCloseableNode node;

	@BeforeEach
	public void setUp() throws Exception {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	@AfterEach
	public void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	private NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("proxies")
						.addProxyMethod(AggregatorProxyMethod.class)
						.build())
				.addModule(ModuleConfig.builder("components")
						.addAmplifier(Aggregate.class)
						.build())
				.build();
	}

	@Test
	public final void testMin() {
		assertEquals(10, aggregatea.min(12, 10, 55));
		assertEquals(10, aggregatea.min(10, 55));
		assertEquals(10, aggregatea.min(10));
	}

	@Test
	public final void testMax() {
		assertEquals(55, aggregatea.max(12, 10, 55));
		assertEquals(55, aggregatea.max(10, 55));
		assertEquals(55, aggregatea.max(55));
	}

	@Test
	public final void testCount() {
		assertEquals(3, aggregatea.count(12, 10, 55));
		assertEquals(2, aggregatea.count(10, 55));
		assertEquals(1, aggregatea.count(55));
	}
}
