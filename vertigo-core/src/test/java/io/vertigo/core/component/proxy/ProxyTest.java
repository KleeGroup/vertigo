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
package io.vertigo.core.component.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.core.component.proxy.data.Aggregate;
import io.vertigo.core.component.proxy.data.AggregatorProxyMethod;

public final class ProxyTest extends AbstractTestCaseJU5 {

	@Inject
	private Aggregate aggregatea;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("proxies")
						.addProxyMethod(AggregatorProxyMethod.class)
						.build())
				.addModule(ModuleConfig.builder("components")
						.addProxy(Aggregate.class)
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
